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

package com.googlesource.gerrit.plugins.webhooks;

import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_MAX_TRIES;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_RETRY_INTERVAL;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_SSL_VERIFY;
import static com.googlesource.gerrit.plugins.webhooks.Configuration.DEFAULT_TIMEOUT_MS;
import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.CONNECTION_TIMEOUT;
import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.MAX_TRIES;
import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.REMOTE;
import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.RETRY_INTERVAL;
import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.SOCKET_TIMEOUT;
import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.SSL_VERIFY;

import com.google.common.base.Strings;
import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.git.meta.VersionedMetaData;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.webhooks.rest.UpsertRemote;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Config;

public class WebhooksConfig extends VersionedMetaData {

  private final String cfgFileName;
  private Config cfg;

  @Inject
  WebhooksConfig(@WebhooksConfigFileName String cfgFileName) {
    this.cfgFileName = cfgFileName;
  }

  @Override
  protected String getRefName() {
    return RefNames.REFS_CONFIG;
  }

  @Override
  protected void onLoad() throws IOException, ConfigInvalidException {
    cfg = readConfig(cfgFileName);
  }

  @Override
  protected boolean onSave(CommitBuilder commit) throws IOException, ConfigInvalidException {
    if (Strings.isNullOrEmpty(commit.getMessage())) {
      commit.setMessage("Updated webhooks\n");
    }
    saveConfig(cfgFileName, cfg);
    return true;
  }

  public void upsertRemote(String name, UpsertRemote.Input in) {
    cfg.setString(REMOTE, name, "url", in.url);
    setStringList(name, "event", in.events);
    setInt(name, CONNECTION_TIMEOUT, in.connectionTimeout, DEFAULT_TIMEOUT_MS);
    setInt(name, SOCKET_TIMEOUT, in.socketTimeout, DEFAULT_TIMEOUT_MS);
    setInt(name, MAX_TRIES, in.maxTries, DEFAULT_MAX_TRIES);
    setInt(name, RETRY_INTERVAL, in.retryInterval, DEFAULT_RETRY_INTERVAL);
    setBoolean(name, SSL_VERIFY, in.sslVerify, DEFAULT_SSL_VERIFY);
  }

  private void setStringList(String remoteName, String key, List<String> value) {
    if (value == null) {
      return;
    }

    if (value.isEmpty()) {
      cfg.unset(REMOTE, remoteName, key);
    } else {
      cfg.setStringList(REMOTE, remoteName, key, value);
    }
  }

  private void setInt(String remoteName, String key, Integer value, int defaultValue) {
    if (value == null) {
      return;
    }

    if (value == defaultValue) {
      cfg.unset(REMOTE, remoteName, key);
    } else {
      cfg.setInt(REMOTE, remoteName, key, value);
    }
  }

  private void setBoolean(String remoteName, String key, Boolean value, boolean defaultValue) {
    if (value == null) {
      return;
    }

    if (value == defaultValue) {
      cfg.unset(REMOTE, remoteName, key);
    } else {
      cfg.setBoolean(REMOTE, remoteName, key, value);
    }
  }

  public void deleteRemote(String remoteName) {
    cfg.unsetSection(REMOTE, remoteName);
  }
}
