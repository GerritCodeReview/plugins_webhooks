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

import static com.googlesource.gerrit.plugins.webhooks.processors.JenkinsEventProcessor.TYPE;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.webhooks.EventProcessor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;

public class FactoryProvider {
  public static final String SECTION = "remote";

  private final Map<String, EventProcessor.Factory> factories;

  @Inject
  protected FactoryProvider(DynamicSet<EventProcessor.Factory> factories) {
    this.factories = new HashMap<>();
    factories.forEach(
        (factory) -> {
          setupProcessor(factory);
        });
  }

  public Optional<EventProcessor.Factory> getFactory(Config cfg, String remote) {
    String type = Optional.ofNullable(cfg.getString(SECTION, remote, "type")).orElse(TYPE);
    return Optional.ofNullable(factories.get(type.toLowerCase()));
  }

  protected void setupProcessor(EventProcessor.Factory factory) {
    factories.put(factory.type().toLowerCase(), factory);
  }
}
