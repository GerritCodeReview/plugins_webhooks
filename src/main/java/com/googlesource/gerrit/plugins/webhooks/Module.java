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

import static com.googlesource.gerrit.plugins.webhooks.DefaultHttpClientProvider.DEFAULT;
import static com.googlesource.gerrit.plugins.webhooks.SslVerifyingHttpClientProvider.SSL_VERIFY;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.events.EventListener;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.googlesource.gerrit.plugins.webhooks.rest.WebhooksRestModule;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.http.impl.client.CloseableHttpClient;

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
        .toProvider(ExecutorProvider.class)
        .in(Scopes.SINGLETON);

    factory(PostTask.Factory.class);
    factory(RemoteConfig.Factory.class);
    factory(HttpSession.Factory.class);

    DynamicSet.bind(binder(), EventListener.class).to(EventHandler.class);

    bind(CloseableHttpClient.class)
        .annotatedWith(Names.named(DEFAULT))
        .toProvider(DefaultHttpClientProvider.class)
        .in(Scopes.SINGLETON);
    bind(CloseableHttpClient.class)
        .annotatedWith(Names.named(SSL_VERIFY))
        .toProvider(SslVerifyingHttpClientProvider.class)
        .in(Scopes.SINGLETON);

    install(processors);
    install(new WebhooksRestModule());
  }

  @Provides
  @WebhooksConfigFileName
  String getWebhooksConfigFileName(@PluginName String pluginName) {
    return pluginName + ".config";
  }
}
