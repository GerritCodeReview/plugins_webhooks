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

import org.apache.http.config.Registry;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

class HttpClientConnectionManagerProvider implements Provider<HttpClientConnectionManager> {
  interface Factory {
    HttpClientConnectionManagerProvider create(Registry<ConnectionSocketFactory> socketFactoryRegistry);
  }

  private static final int CONNECTIONS_PER_ROUTE = 100;
  // Up to 2 target instances with the max number of connections per host:
  private static final int MAX_CONNECTIONS = 2 * CONNECTIONS_PER_ROUTE;
  private static final int MAX_CONNECTION_INACTIVITY = 10000;

  private final Registry<ConnectionSocketFactory> socketFactoryRegistry;

  @Inject
  HttpClientConnectionManagerProvider(@Assisted Registry<ConnectionSocketFactory> socketFactoryRegistry) {
    this.socketFactoryRegistry = socketFactoryRegistry;
  }

  @Override
  public HttpClientConnectionManager get() {
    PoolingHttpClientConnectionManager connManager =
        new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    connManager.setDefaultMaxPerRoute(CONNECTIONS_PER_ROUTE);
    connManager.setMaxTotal(MAX_CONNECTIONS);
    connManager.setValidateAfterInactivity(MAX_CONNECTION_INACTIVITY);
    return connManager;
  }
}
