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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PostTask implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(PostTask.class);

  interface Factory {
    PostTask create(RemoteConfig remote, EventProcessor processor);
  }

  private final ScheduledExecutorService executor;
  private final HttpSession session;
  private final RemoteConfig remote;
  private final Supplier<Optional<EventProcessor.Result>> processor;
  private int execCnt;

  @AssistedInject
  public PostTask(
      @WebHooksExecutor ScheduledExecutorService executor,
      HttpSession session,
      @Assisted RemoteConfig remote,
      @Assisted EventProcessor processor) {
    this.executor = executor;
    this.session = session;
    this.remote = remote;
    this.processor =
        Suppliers.memoize(
            new Supplier<Optional<EventProcessor.Result>>() {
              @Override
              public Optional<EventProcessor.Result> get() {
                return processor.process();
              }
            });
  }

  void schedule() {
    executor.execute(this);
  }

  private void reschedule() {
    executor.schedule(this, remote.getRetryInterval(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void run() {
    try {
      Optional<EventProcessor.Result> content = processor.get();
      if (!content.isPresent()) {
        log.debug("No content. Webhook [{}] skipped.", remote.getUrl());
        return;
      }

      execCnt++;
      HttpResult result = session.post(remote.getUrl(), content.get().headers,
          remote, content.get().body);
      if (!result.successful && execCnt < remote.getMaxTries()) {
        logRetry(result.message);
        reschedule();
      }
    } catch (IOException e) {
      if (isRecoverable(e) && execCnt < remote.getMaxTries()) {
        logRetry(e);
        reschedule();
      } else {
        log.error("Failed to post: {}", toString(), e);
      }
    }
  }

  private boolean isRecoverable(IOException e) {
    return !(e instanceof SSLException);
  }

  private void logRetry(String reason) {
    if (log.isDebugEnabled()) {
      log.debug("Retrying {} in {}ms. Reason: {}", toString(), remote.getRetryInterval(), reason);
    }
  }

  private void logRetry(Throwable cause) {
    if (log.isDebugEnabled()) {
      log.debug("Retrying {} in {}ms. Cause: {}", toString(), remote.getRetryInterval(), cause);
    }
  }

  @Override
  public String toString() {
    Optional<EventProcessor.Result> content = processor.get();
    return content.isPresent() ? content.get().toString() : "no content";
  }
}
