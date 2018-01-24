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

package com.googlesource.gerrit.plugins.webhooks.processors;

import com.google.common.base.Strings;
import com.google.gerrit.server.events.ProjectEvent;
import com.googlesource.gerrit.plugins.webhooks.EventProcessor;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import java.util.Optional;

public abstract class AbstractEventProcessor implements EventProcessor {
  @Override
  public Optional<EventProcessor.Request> process(ProjectEvent event, RemoteConfig remote) {
    if (!shouldProcess(event, remote)) {
      return Optional.empty();
    }

    return doProcess(event, remote);
  }

  protected abstract Optional<EventProcessor.Request> doProcess(
      ProjectEvent event, RemoteConfig remote);

  protected boolean shouldProcess(ProjectEvent event, RemoteConfig remote) {
    String[] wantedEvents = remote.getEvents();
    if (wantedEvents.length == 0) {
      return true;
    }

    for (String type : wantedEvents) {
      if (!Strings.isNullOrEmpty(type) && type.equals(event.getType())) {
        return true;
      }
    }

    return false;
  }
}
