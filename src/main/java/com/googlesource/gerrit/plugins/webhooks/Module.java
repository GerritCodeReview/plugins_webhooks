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

import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gerrit.common.EventListener;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.Inject;
import com.google.inject.Scopes;

public class Module extends FactoryModule {
  private final ProcessorModule processors;

  @Inject
  public Module(ProcessorModule processors) {
    this.processors = processors;
  }

  @Override
  protected void configure() {
    bind(ScheduledExecutorService.class)
        .annotatedWith(WebHooksExecutor.class)
        .toProvider(ExecutorProvider.class);
    bind(CloseableHttpClient.class).toProvider(HttpClientProvider.class).in(Scopes.SINGLETON);
    factory(PostTask.Factory.class);
    DynamicSet.bind(binder(), EventListener.class).to(EventHandler.class);

    install(processors);
  }
}
