// Copyright (C) 2018 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.webhooks.rest;

import static com.google.common.truth.Truth.assertThat;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_MAX_TRIES;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_RETRY_INTERVAL;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_SSL_VERIFY;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_TIMEOUT_MS;
import static com.googlesource.gerrit.plugins.webhooks.rest.GetRemoteIT.asMap;
import static com.googlesource.gerrit.plugins.webhooks.rest.GetRemoteIT.asRemoteInfo;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.RestResponse;
import com.google.gerrit.acceptance.TestPlugin;
import org.junit.Before;
import org.junit.Test;

@TestPlugin(name = "webhooks", sysModule = "com.googlesource.gerrit.plugins.webhooks.Module")
public class UpdateRemoteIT extends LightweightPluginDaemonTest {

  private String fooEndpoint;

  @Override
  @Before
  public void setUpTestPlugin() throws Exception {
    super.setUpTestPlugin();
    fooEndpoint = String.format("/config/server/webhooks~projects/%s/remotes/foo", project.get());
  }

  @Test
  public void createWebhook() throws Exception {
    RemoteInfo info = newRemoteInfo("https://foo.org/");
    RestResponse res = adminRestSession.put(fooEndpoint, info);
    assertThat(res.getStatusCode()).isEqualTo(SC_CREATED);
    assertThat(info).isEqualTo(asRemoteInfo(res.getEntityContent()));
  }

  @Test
  public void updateWebhook() throws Exception {
    createWebhook();
    RemoteInfo info = newRemoteInfo("https://foo.org/gerrit-events");
    RestResponse res = adminRestSession.put(fooEndpoint, info);
    assertThat(res.getStatusCode()).isEqualTo(SC_OK);
    assertThat(info).isEqualTo(asRemoteInfo(res.getEntityContent()));
  }

  @Test
  public void deleteRemote() throws Exception {
    createWebhook();
    RestResponse res = adminRestSession.delete(fooEndpoint);
    assertThat(res.getStatusCode()).isEqualTo(SC_NO_CONTENT);

    RestResponse response =
        adminRestSession.get(
            String.format("/config/server/webhooks~projects/%s/remotes/", project.get()));
    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(asMap(response.getEntityContent()).size()).isEqualTo(0);
  }

  private RemoteInfo newRemoteInfo(String url) {
    RemoteInfo info = new RemoteInfo();
    info.url = url;
    info.events = ImmutableList.of();
    info.connectionTimeout = DEFAULT_TIMEOUT_MS;
    info.socketTimeout = DEFAULT_TIMEOUT_MS;
    info.maxTries = DEFAULT_MAX_TRIES;
    info.retryInterval = DEFAULT_RETRY_INTERVAL;
    info.sslVerify = DEFAULT_SSL_VERIFY;
    return info;
  }
}
