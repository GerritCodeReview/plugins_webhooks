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

import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.REMOTE;

import com.google.common.base.Strings;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.inject.Inject;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;

class EventHandler implements EventListener {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final Configuration global;
  private final PluginConfigFactory configFactory;
  private final String pluginName;
  private final RemoteConfig.Factory remoteFactory;
  private final PostTask.Factory taskFactory;
  private final EventProcessor processor;

  @Inject
  EventHandler(
      Configuration global,
      PluginConfigFactory configFactory,
      @PluginName String pluginName,
      RemoteConfig.Factory remoteFactory,
      PostTask.Factory taskFactory,
      EventProcessor processor) {
    this.global = global;
    this.configFactory = configFactory;
    this.pluginName = pluginName;
    this.remoteFactory = remoteFactory;
    this.taskFactory = taskFactory;
    this.processor = processor;
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
      log.atWarning().log(
          "Ignoring event for a non-existing project %s, %s",
          projectEvent.getProjectNameKey().get(), projectEvent);
      return;
    }

    for (String name : cfg.getSubsections(REMOTE)) {
      RemoteConfig remote = remoteFactory.create(cfg, name);
      if (Strings.isNullOrEmpty(remote.getUrl())) {
        log.atWarning().log("remote.%s.url not defined, skipping this remote", name);
        continue;
      }
      if (!isUrlAllowed(remote.getUrl())) {
        log.atWarning().log(
            "remote.%s.url does not match any allowed URL patterns, skipping this remote", name);
        continue;
      }
      Optional<EventProcessor.Request> content = processor.process(projectEvent, remote);
      if (content.isEmpty()) {
        log.atFine().log(
            "No content (rejected by processing). Webhook [%s] skipped.", remote.getUrl());
        continue;
      }
      taskFactory.create(projectEvent, remote, content.get()).schedule();
    }
  }

  private boolean isUrlAllowed(String url) {
    return global.getAllowedUrlPatterns().isEmpty()
        || global.getAllowedUrlPatterns().stream()
            .anyMatch(pattern -> pattern.matcher(url).matches());
  }
}
