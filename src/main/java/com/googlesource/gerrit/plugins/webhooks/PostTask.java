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
import com.google.gerrit.server.events.ProjectEvent;
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
    PostTask create(ProjectEvent event, RemoteConfig remote);
  }

  private final ScheduledExecutorService executor;
  private final Supplier<HttpSession> session;
  private final RemoteConfig remote;
  private final ProjectEvent event;
  private final Supplier<Optional<EventProcessor.Request>> processor;
  private int execCnt;

  @AssistedInject
  public PostTask(
      @WebHooksExecutor ScheduledExecutorService executor,
      HttpSession.Factory session,
      EventProcessor processor,
      @Assisted ProjectEvent event,
      @Assisted RemoteConfig remote) {
    this.executor = executor;
    this.event = event;
    this.remote = remote;
    // postpone creation of HttpSession so that it is obtained only when processor
    // returns non-empty content
    this.session = Suppliers.memoize(() -> session.create(remote));
    this.processor = Suppliers.memoize(() -> processor.process(event, remote));
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
      Optional<EventProcessor.Request> content = processor.get();
      if (!content.isPresent()) {
        log.debug("No content. Webhook [{}] skipped.", remote.getUrl());
        return;
      }

      execCnt++;
      HttpResult result = session.get().post(remote, content.get());
      if (!result.successful && execCnt < remote.getMaxTries()) {
        logRetry(result.message);
        reschedule();
      }
    } catch (Throwable e) {
      if (isRecoverable(e) && execCnt < remote.getMaxTries()) {
        logRetry(e);
        reschedule();
      } else {
        log.error("Failed to post: {}", toString(), e);
      }
    }
  }

  private boolean isRecoverable(Throwable e) {
    return (e instanceof IOException) && !(e instanceof SSLException);
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
    return new StringBuilder()
        .append("Processing event: ")
        .append(event.getType())
        .append(" for project: ")
        .append(event.getProjectNameKey().get())
        .append(" for remote: ")
        .append(remote.getUrl())
        .toString();
  }
}
