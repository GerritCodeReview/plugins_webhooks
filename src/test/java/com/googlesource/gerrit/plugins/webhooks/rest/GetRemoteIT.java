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
import static com.google.gerrit.reviewdb.client.RefNames.REFS_CONFIG;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_MAX_TRIES;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_RETRY_INTERVAL;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_SSL_VERIFY;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_TIMEOUT_MS;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.PushOneCommit;
import com.google.gerrit.acceptance.RestResponse;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.transport.RefSpec;
import org.junit.Test;

@TestPlugin(name = "webhooks", sysModule = "com.googlesource.gerrit.plugins.webhooks.Module")
public class GetRemoteIT extends LightweightPluginDaemonTest {

  @Test
  public void noWebhooks() throws IOException {
    RestResponse response =
        adminRestSession.get(
            String.format("/config/server/webhooks~projects/%s/remotes/", project.get()));
    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(asMap(response.getEntityContent()).size()).isEqualTo(0);
  }

  @Test
  public void testGet() throws Exception {
    git().fetch().setRefSpecs(new RefSpec(REFS_CONFIG + ":" + REFS_CONFIG)).call();
    testRepo.reset(REFS_CONFIG);

    RemoteInfo expected;
    String getFoo = String.format("/config/server/webhooks~projects/%s/remotes/foo", project.get());

    String webhooksConfig = "[remote \"foo\"]\n  url = https://foo.org/\n";
    expected = newRemoteInfo("https://foo.org/");
    pushAndVerify(getFoo, webhooksConfig, expected);

    webhooksConfig = webhooksConfig + "  event = \"ref-updated\"\n";
    expected.events = ImmutableList.of("ref-updated");
    pushAndVerify(getFoo, webhooksConfig, expected);

    webhooksConfig = webhooksConfig + "  event = \"patchset-created\"\n";
    expected.events = ImmutableList.of("ref-updated", "patchset-created");
    pushAndVerify(getFoo, webhooksConfig, expected);

    webhooksConfig = webhooksConfig + "  connectionTimeout = 123\n";
    expected.connectionTimeout = 123;
    pushAndVerify(getFoo, webhooksConfig, expected);

    webhooksConfig = webhooksConfig + "  socketTimeout = 234\n";
    expected.socketTimeout = 234;
    pushAndVerify(getFoo, webhooksConfig, expected);

    webhooksConfig = webhooksConfig + "  maxTries = 345\n";
    expected.maxTries = 345;
    pushAndVerify(getFoo, webhooksConfig, expected);

    webhooksConfig = webhooksConfig + "  retryInterval = 456\n";
    expected.retryInterval = 456;
    pushAndVerify(getFoo, webhooksConfig, expected);

    webhooksConfig = webhooksConfig + "  sslVerify = " + !DEFAULT_SSL_VERIFY;
    expected.sslVerify = !DEFAULT_SSL_VERIFY;
    pushAndVerify(getFoo, webhooksConfig, expected);
  }

  private void pushAndVerify(String endpoint, String webhooksConfig, RemoteInfo expected)
      throws Exception {
    push(webhooksConfig);
    RestResponse res = adminRestSession.get(endpoint);
    assertThat(res.getStatusCode()).isEqualTo(200);
    assertThat(expected).isEqualTo(asRemoteInfo(res.getEntityContent()));
  }

  @Test
  public void testList() throws Exception {
    git().fetch().setRefSpecs(new RefSpec(REFS_CONFIG + ":" + REFS_CONFIG)).call();
    testRepo.reset(REFS_CONFIG);

    Map<String, RemoteInfo> expected = new HashMap<>();
    String listAll = String.format("/config/server/webhooks~projects/%s/remotes/", project.get());

    String webhooksConfig = "[remote \"foo\"]\n  url = https://foo.org/\n";
    expected.put("foo", newRemoteInfo("https://foo.org/"));
    pushAndVerifyList(listAll, webhooksConfig, expected);

    webhooksConfig =
        webhooksConfig + "[remote \"bar\"]\n  url = https://bar.org/\n  event = \"ref-updated\"\n";
    RemoteInfo barInfo = newRemoteInfo("https://bar.org/");
    barInfo.events = ImmutableList.of("ref-updated");
    expected.put("bar", barInfo);
    pushAndVerifyList(listAll, webhooksConfig, expected);

    webhooksConfig =
        webhooksConfig
            + "[remote \"qux\"]\n  url = https://qux.org/\n  event = \"patchset-created\"\n";
    RemoteInfo quxInfo = newRemoteInfo("https://qux.org/");
    quxInfo.events = ImmutableList.of("patchset-created");
    expected.put("qux", quxInfo);
    pushAndVerifyList(listAll, webhooksConfig, expected);
  }

  private void pushAndVerifyList(
      String endpoint, String webhooksConfig, Map<String, RemoteInfo> expected) throws Exception {
    push(webhooksConfig);
    RestResponse res = adminRestSession.get(endpoint);
    assertThat(res.getStatusCode()).isEqualTo(SC_OK);
    assertThat(expected).isEqualTo(asMap(res.getEntityContent()));
  }

  private void push(String content) throws Exception {
    PushOneCommit push =
        pushFactory.create(
            admin.getIdent(), testRepo, "Create webhooks", "webhooks.config", content);
    push.to(REFS_CONFIG);
  }

  static Map<String, RemoteInfo> asMap(String json) {
    return newGson().fromJson(json, new TypeToken<Map<String, RemoteInfo>>() {}.getType());
  }

  static RemoteInfo asRemoteInfo(String json) {
    return newGson().fromJson(json, new TypeToken<RemoteInfo>() {}.getType());
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
