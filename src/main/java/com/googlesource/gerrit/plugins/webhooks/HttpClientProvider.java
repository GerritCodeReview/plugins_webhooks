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

import com.google.inject.Provider;
import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides an HTTP client with SSL capabilities. */
abstract class HttpClientProvider implements Provider<CloseableHttpClient> {
  private static final Logger log = LoggerFactory.getLogger(HttpClientProvider.class);
  private static final int ERROR_CODES = 500;

  private final Configuration cfg;
  private final Provider<HttpClientConnectionManager> mgr;

  protected HttpClientProvider(Configuration cfg, Provider<HttpClientConnectionManager> mgr) {
    this.cfg = cfg;
    this.mgr = mgr;
  }

  @Override
  public CloseableHttpClient get() {
    return HttpClients.custom()
        .setConnectionManager(mgr.get())
        .setDefaultRequestConfig(customRequestConfig())
        .setServiceUnavailableRetryStrategy(customServiceUnavailRetryStrategy())
        .build();
  }

  private RequestConfig customRequestConfig() {
    return RequestConfig.custom()
        .setConnectTimeout(cfg.getConnectionTimeout())
        .setSocketTimeout(cfg.getSocketTimeout())
        .setConnectionRequestTimeout(cfg.getConnectionTimeout())
        .build();
  }

  private ServiceUnavailableRetryStrategy customServiceUnavailRetryStrategy() {
    return new ServiceUnavailableRetryStrategy() {
      @Override
      public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
        if (executionCount > cfg.getMaxTries()) {
          return false;
        }
        if (response.getStatusLine().getStatusCode() >= ERROR_CODES) {
          logRetry(response.getStatusLine().getReasonPhrase(), context);
          return true;
        }
        return false;
      }

      @Override
      public long getRetryInterval() {
        return cfg.getRetryInterval();
      }
    };
  }

  private void logRetry(String cause, HttpContext context) {
    if (log.isDebugEnabled()) {
      log.debug(
          "Retrying request caused by '"
              + cause
              + "', request: '"
              + context.getAttribute("http.request")
              + "'");
    }
  }
}
