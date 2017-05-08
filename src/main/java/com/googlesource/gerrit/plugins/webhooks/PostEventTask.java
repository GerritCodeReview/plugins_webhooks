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

import com.google.common.base.Supplier;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.events.SupplierSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

class PostEventTask implements Runnable {
  private static final Logger log = LoggerFactory
      .getLogger(PostEventTask.class);

  private static Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Supplier.class, new SupplierSerializer())
        .create();

  interface Factory {
    PostEventTask create(String url, ProjectEvent projectEvent);
  }

  private final Executor executor;
  private final HttpSession session;
  private final String url;
  private final ProjectEvent projectEvent;

  @AssistedInject
  public PostEventTask(@WebHooksExecutor Executor executor,
      HttpSession session,
      @Assisted String url,
      @Assisted ProjectEvent projectEvent) {
    this.executor = executor;
    this.session = session;
    this.url = url;
    this.projectEvent = projectEvent;
  }

  void schedule() {
    executor.execute(this);
  }

  @Override
  public void run() {
    String serializedEvent = GSON.toJson(projectEvent);
    try {
      session.post(url, serializedEvent);
    } catch (IOException e) {
      log.error("Coulnd't post event: " + projectEvent, e);
    }
  }

  @Override
  public String toString() {
    return String.format("%s:%s > %s",
        projectEvent.type, projectEvent.getProjectNameKey().get(), url);
  }
}
