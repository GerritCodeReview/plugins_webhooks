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

import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

class HttpSession {
  interface Factory {
    HttpSession create(RemoteConfig remote);
  }

  private final CloseableHttpClient httpClient;

  @Inject
  HttpSession(
      @Named(DEFAULT) Provider<CloseableHttpClient> defaultClientProvider,
      @Named(SSL_VERIFY) Provider<CloseableHttpClient> sslVerifyingClientProvider,
      @Assisted RemoteConfig remote) {
    this.httpClient =
        remote.getSslVerify() ? sslVerifyingClientProvider.get() : defaultClientProvider.get();
  }

  HttpResult post(RemoteConfig remote, EventProcessor.Request request) throws IOException {
    HttpPost post = new HttpPost(remote.getUrl());
    post.addHeader("Content-Type", MediaType.JSON_UTF_8.toString());
    post.setConfig(getConfig(remote));
    request
        .headers
        .entrySet()
        .stream()
        .forEach(
            e -> {
              post.addHeader(e.getKey(), e.getValue());
            });
    post.setEntity(new StringEntity(request.body, StandardCharsets.UTF_8));
    return httpClient.execute(post, new HttpResponseHandler());
  }

  private RequestConfig getConfig(RemoteConfig remote) {
    return RequestConfig.custom()
        .setConnectTimeout(remote.getConnectionTimeout())
        .setConnectionRequestTimeout(remote.getConnectionTimeout())
        .setSocketTimeout(remote.getSocketTimeout())
        .build();
  }
}
