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

import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

class HttpSession {
  private final CloseableHttpClient httpClient;

  @Inject
  HttpSession(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  HttpResult post(String endpoint, Map<String, String> headers, String content) throws IOException {
    HttpPost post = new HttpPost(endpoint);
    post.addHeader("Content-Type", MediaType.JSON_UTF_8.toString());
    headers.entrySet().stream().forEach(e -> {
      post.addHeader(e.getKey(), e.getValue());
    });
    post.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
    return httpClient.execute(post, new HttpResponseHandler());
  }
}
