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
import com.google.common.collect.ImmutableMap;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.events.SupplierSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.webhooks.EventProcessor;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import java.util.Optional;

public class GerritEventProcessor extends AbstractEventProcessor {
  private static Gson GSON =
      new GsonBuilder().registerTypeAdapter(Supplier.class, new SupplierSerializer()).create();

  private final String canonicalWebUrl;

  @Inject
  GerritEventProcessor(@CanonicalWebUrl String canonicalWebUrl) {
    this.canonicalWebUrl = canonicalWebUrl;
  }

  @Override
  public Optional<EventProcessor.Request> doProcess(ProjectEvent event, RemoteConfig remote) {
    return Optional.of(
        new EventProcessor.Request(
            GSON.toJson(event), ImmutableMap.of("X-Origin-Url", canonicalWebUrl)));
  }
}
