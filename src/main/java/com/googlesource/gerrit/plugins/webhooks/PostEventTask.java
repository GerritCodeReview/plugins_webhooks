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

import java.io.IOException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

class PostEventTask implements Runnable {
  private static final Logger log = LoggerFactory
      .getLogger(PostEventTask.class);

  interface Factory {
    PostEventTask create(@Assisted("url") String url, @Assisted("event") String eventJson);
  }

  private final Executor executor;
  private final HttpSession session;
  private final String url;
  private final String eventJson;

  @AssistedInject
  public PostEventTask(@WebHooksExecutor Executor executor,
      HttpSession session,
      @Assisted("url") String url,
      @Assisted("event") String eventJson) {
    this.executor = executor;
    this.session = session;
    this.url = url;
    this.eventJson = eventJson;
  }

  void schedule() {
    executor.execute(this);
  }

  @Override
  public void run() {
    try {
      session.post(url, eventJson);
    } catch (IOException e) {
      log.error("Couldn't post event {}", eventJson, e);
    }
  }

  @Override
  public String toString() {
    return eventJson;
  }
}
