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

import com.google.common.collect.ImmutableSet;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.events.RefUpdatedEvent;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.webhooks.EventProcessor;
import com.googlesource.gerrit.plugins.webhooks.processors.github.HeadersProvider;
import com.googlesource.gerrit.plugins.webhooks.processors.github.PushEvent;
import com.googlesource.gerrit.plugins.webhooks.processors.github.PushEventTransformer;
import com.googlesource.gerrit.plugins.webhooks.processors.github.RefUpdatedEventClassifier;
import java.util.Optional;

public class GitHubEventProcessor extends AbstractEventProcessor {
  interface AssistedFactory {
    GitHubEventProcessor create(@Assisted ProjectEvent event);
  }

  public static class Factory implements EventProcessor.Factory {
    private final AssistedFactory factory;

    @Inject
    Factory(AssistedFactory factory) {
      this.factory = factory;
    }

    public static Module module() {
      return new FactoryModule() {
        @Override
        protected void configure() {
          factory(AssistedFactory.class);
          DynamicSet.bind(binder(), EventProcessor.Factory.class)
              .to(GitHubEventProcessor.Factory.class);
        }
      };
    }

    @Override
    public EventProcessor create(ProjectEvent event) {
      return factory.create(event);
    }

    @Override
    public String type() {
      return TYPE;
    }
  }

  private static final String TYPE = "github";
  private static Gson GSON =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();
  private static final ImmutableSet<String> SUPPORTED = ImmutableSet.of("ref-updated");

  private final RefUpdatedEventClassifier classifier;
  private final PushEventTransformer toRefUpdated;
  private final HeadersProvider headers;

  @Inject
  protected GitHubEventProcessor(
      RefUpdatedEventClassifier classifier,
      PushEventTransformer toRefUpdated,
      HeadersProvider headers,
      @Assisted ProjectEvent event) {
    super(event);
    this.classifier = classifier;
    this.toRefUpdated = toRefUpdated;
    this.headers = headers;
  }

  @Override
  public boolean shouldPost(String[] wantedEvents) {
    if (!SUPPORTED.contains(event.getType())) {
      return false;
    }

    if (classifier.ignore((RefUpdatedEvent) event)) {
      return false;
    }

    return super.shouldPost(wantedEvents);
  }

  @Override
  public Optional<EventProcessor.Output> process() {
    Optional<PushEvent> gitHubEvent = toRefUpdated.apply(((RefUpdatedEvent) event));
    if (gitHubEvent.isPresent()) {
      return Optional.of(
          new EventProcessor.Output(
              GSON.toJson(gitHubEvent.get()), headers.get(gitHubEvent.get())));
    }

    return Optional.empty();
  }
}
