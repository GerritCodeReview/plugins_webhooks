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

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.apache.http.impl.client.CloseableHttpClient;

class HttpClientConfigurator implements Function<RemoteConfig, CloseableHttpClient> {
  private final Provider<CloseableHttpClient> defaultClientProvider;
  private final Provider<CloseableHttpClient> sslVerifyingClientProvider;

  @Inject
  HttpClientConfigurator(
      @Named(DEFAULT) Provider<CloseableHttpClient> defaultClientProvider,
      @Named(SSL_VERIFY) Provider<CloseableHttpClient> sslVerifyingClientProvider) {
    this.defaultClientProvider = defaultClientProvider;
    this.sslVerifyingClientProvider = sslVerifyingClientProvider;
  }

  @Override
  public CloseableHttpClient apply(RemoteConfig remote) {
    return remote.getDisableSslVerification()
        ? defaultClientProvider.get()
        : sslVerifyingClientProvider.get();
  }
}
