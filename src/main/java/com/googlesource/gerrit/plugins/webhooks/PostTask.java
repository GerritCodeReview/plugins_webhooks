// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.webhooks;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PostTask implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(PostTask.class);

  interface Factory {
    PostTask create(@Assisted("url") String url, @Assisted("body") String body);
  }

  private final ScheduledExecutorService executor;
  private final HttpSession session;
  private final Configuration cfg;
  private final String url;
  private final String body;
  private int execCnt;

  @AssistedInject
  public PostTask(
      @WebHooksExecutor ScheduledExecutorService executor,
      HttpSession session,
      Configuration cfg,
      @Assisted("url") String url,
      @Assisted("body") String body) {
    this.executor = executor;
    this.session = session;
    this.cfg = cfg;
    this.url = url;
    this.body = body;
  }

  void schedule() {
    executor.execute(this);
  }

  private void reschedule() {
    executor.schedule(this, cfg.getRetryInterval(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void run() {
    try {
      execCnt++;
      HttpResult result = session.post(url, body);
      if (!result.successful && execCnt < cfg.getMaxTries()) {
        logRetry(result.message);
        reschedule();
      }
    } catch (IOException e) {
      if (isRecoverable(e) && execCnt < cfg.getMaxTries()) {
        logRetry(e);
        reschedule();
      } else {
        log.error("Failed to post: {}", body, e);
      }
    }
  }

  private boolean isRecoverable(IOException e) {
    return !(e instanceof SSLException);
  }

  private void logRetry(String reason) {
    if (log.isDebugEnabled()) {
      log.debug("Retrying {} in {}ms. Reason: {}", toString(), cfg.getRetryInterval(), reason);
    }
  }

  private void logRetry(Throwable cause) {
    if (log.isDebugEnabled()) {
      log.debug("Retrying {} in {}ms. Cause: {}", toString(), cfg.getRetryInterval(), cause);
    }
  }

  @Override
  public String toString() {
    return body;
  }
}
