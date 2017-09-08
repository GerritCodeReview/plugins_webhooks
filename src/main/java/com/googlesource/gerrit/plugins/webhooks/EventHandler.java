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

import static com.googlesource.gerrit.plugins.webhooks.processors.FactoryProvider.SECTION;

import com.google.common.base.Strings;
import com.google.gerrit.common.EventListener;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.webhooks.processors.FactoryProvider;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventHandler implements EventListener {
  private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

  private final PluginConfigFactory configFactory;
  private final String pluginName;
  private final PostTask.Factory taskFactory;
  private final FactoryProvider provider;

  @Inject
  EventHandler(
      PluginConfigFactory configFactory,
      @PluginName String pluginName,
      PostTask.Factory taskFactory,
      FactoryProvider provider) {
    this.configFactory = configFactory;
    this.pluginName = pluginName;
    this.taskFactory = taskFactory;
    this.provider = provider;
  }

  @Override
  public void onEvent(Event event) {
    if (!(event instanceof ProjectEvent)) {
      return;
    }

    ProjectEvent projectEvent = (ProjectEvent) event;
    Config cfg;
    try {
      cfg =
          configFactory.getProjectPluginConfigWithInheritance(
              projectEvent.getProjectNameKey(), pluginName);
    } catch (NoSuchProjectException e) {
      log.warn(
          "Ignoring event for a non-existing project {}, {}",
          projectEvent.getProjectNameKey().get(),
          projectEvent);
      return;
    }

    for (String name : cfg.getSubsections(SECTION)) {
      String url = cfg.getString(SECTION, name, "url");
      if (Strings.isNullOrEmpty(url)) {
        log.warn("remote.{}.url not defined, skipping this remote", name);
        continue;
      }

      Optional<EventProcessor.Factory> factory = provider.getFactory(cfg, name);
      if (!factory.isPresent()) {
        log.warn("remote.{}.type not recognized, skipping this remote", name);
        continue;
      }

      EventProcessor processor = factory.get().create(projectEvent);
      if (processor.shouldPost(cfg.getStringList(SECTION, name, "event"))) {
        post(url, processor);
      }
    }
  }

  private void post(String url, EventProcessor processor) {
    taskFactory.create(url, processor).schedule();
  }
}
