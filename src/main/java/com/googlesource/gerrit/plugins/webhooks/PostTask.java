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
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

class PostTask implements Runnable {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  interface Factory {
    PostTask create(ProjectEvent event, RemoteConfig remote, EventProcessor.Request content);
  }

  private final ScheduledExecutorService executor;
  private final Supplier<HttpSession> session;
  private final RemoteConfig remote;
  private final ProjectEvent event;
  private final EventProcessor.Request content;
  private int execCnt;

  @AssistedInject
  public PostTask(
      @WebHooksExecutor ScheduledExecutorService executor,
      HttpSession.Factory session,
      @Assisted ProjectEvent event,
      @Assisted RemoteConfig remote,
      @Assisted EventProcessor.Request content) {
    this.executor = executor;
    this.event = event;
    this.remote = remote;
    this.content = content;
    // postpone creation of HttpSession so that it is obtained only when processor
    // returns non-empty content
    this.session = Suppliers.memoize(() -> session.create(remote));
  }

  void schedule() {
    executor.execute(this);
  }

  private void reschedule() {
    @SuppressWarnings("unused")
    ScheduledFuture<?> ignored =
        executor.schedule(this, remote.getRetryInterval(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void run() {
    try {
      execCnt++;
      HttpResult result = session.get().post(remote, content);
      if (!result.successful) {
        if (execCnt < remote.getMaxTries()) {
          logRetry(result.message);
          reschedule();
        } else {
          log.atSevere().log(
              "Failed to post: %s. Exceeded max retries(%d). Reason: %s",
              this, remote.getMaxTries(), result.message);
        }
      }
    } catch (Throwable e) {
      if (isRecoverable(e) && execCnt < remote.getMaxTries()) {
        logRetry(e);
        reschedule();
      } else {
        log.atSevere().withCause(e).log("Failed to post: %s", this);
      }
    }
  }

  private boolean isRecoverable(Throwable e) {
    return (e instanceof IOException) && !(e instanceof SSLException);
  }

  private void logRetry(String reason) {
    log.atFine().log("Retrying %s in %dms. Reason: %s", this, remote.getRetryInterval(), reason);
  }

  private void logRetry(Throwable cause) {
    log.atFine().withCause(cause).log("Retrying %s in %dms", this, remote.getRetryInterval());
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
