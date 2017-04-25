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

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.gerrit.common.EventListener;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.events.SupplierSerializer;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

class EventHandler implements EventListener {
  private static final Logger log = LoggerFactory
      .getLogger(EventHandler.class);

  private final HttpSession session;
  private final PluginConfigFactory configFactory;
  private final String pluginName;
  private final Executor executor;

  private final Gson gson;

  @Inject
  EventHandler(HttpSession session,
      PluginConfigFactory configFactory,
      @PluginName String pluginName,
      @WebHooksExecutor Executor executor) {
    this.session = session;
    this.configFactory = configFactory;
    this.pluginName = pluginName;
    this.executor = executor;
    this.gson = new GsonBuilder()
        .registerTypeAdapter(Supplier.class, new SupplierSerializer())
        .create();
  }

  @Override
  public void onEvent(Event event) {
    if (!(event instanceof ProjectEvent)) {
      return;
    }

    ProjectEvent projectEvent = (ProjectEvent) event;
    Config cfg;
    try {
      cfg = configFactory.getProjectPluginConfigWithInheritance(
          projectEvent.getProjectNameKey(), pluginName);
    } catch (NoSuchProjectException e) {
      log.warn("Ignoring event for a non-existing project {}, {}",
          projectEvent.getProjectNameKey().get(), projectEvent);
      return;
    }

    for (String name : cfg.getSubsections("remote")) {
      String url = cfg.getString("remote", name, "url");
      if (Strings.isNullOrEmpty(url)) {
        continue;
      }

      String[] eventTypes = cfg.getStringList("remote", name, "event");

      if (eventTypes.length == 0) {
        post(url, projectEvent);
      }

      for (String type : eventTypes) {
        if (Strings.isNullOrEmpty(type)) {
          continue;
        }
        if (type.equals(projectEvent.getType())) {
          post(url, projectEvent);
        }
      }
    }
  }

  private void post(final String url, final ProjectEvent projectEvent) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        String serializedEvent = gson.toJson(projectEvent);
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
    });
  }
}
