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

import com.google.common.base.Supplier;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.events.SupplierSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlesource.gerrit.plugins.webhooks.EventProcessor;
import org.eclipse.jgit.lib.Config;

public class GerritEventProcessor extends AbstractEventProcessor {
  public static class Factory implements EventProcessor.Factory {
    @Override
    public GerritEventProcessor create(ProjectEvent event, Config cfg, String name) {
      return new GerritEventProcessor(event, cfg, name);
    }
  }

  static String TYPE = "gerrit";
  private static Gson GSON =
      new GsonBuilder().registerTypeAdapter(Supplier.class, new SupplierSerializer()).create();

  GerritEventProcessor(ProjectEvent event, Config cfg, String name) {
    super(event, cfg, name);
  }

  @Override
  public String process() {
    return GSON.toJson(event);
  }
}
