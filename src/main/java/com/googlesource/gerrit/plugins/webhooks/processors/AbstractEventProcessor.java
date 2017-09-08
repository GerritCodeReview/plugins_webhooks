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
import com.googlesource.gerrit.plugins.webhooks.Configuration;
import com.googlesource.gerrit.plugins.webhooks.EventProcessor;
import org.eclipse.jgit.lib.Config;

public abstract class AbstractEventProcessor implements EventProcessor {
  protected final ProjectEvent event;
  protected final Config cfg;
  protected final String name;

  protected AbstractEventProcessor(ProjectEvent event, Config cfg, String name) {
    this.event = event;
    this.cfg = cfg;
    this.name = name;
  }

  @Override
  public boolean shouldProcess() {
    String[] wantedEvents = cfg.getStringList(Configuration.REMOTE, name, "event");
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
