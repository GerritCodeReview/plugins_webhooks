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

import static com.googlesource.gerrit.plugins.webhooks.RemoteConfig.REMOTE;
import static com.googlesource.gerrit.plugins.webhooks.rest.GetRemote.fromRemoteConfig;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.lib.Config;

@Singleton
public class ListRemotes implements RestReadView<ProjectWebhooksResource> {

  private final PluginConfigFactory configFactory;
  private final RemoteConfig.Factory remoteFactory;
  private final String pluginName;

  @Inject
  ListRemotes(
      PluginConfigFactory configFactory,
      RemoteConfig.Factory remoteFactory,
      @PluginName String pluginName) {
    this.configFactory = configFactory;
    this.remoteFactory = remoteFactory;
    this.pluginName = pluginName;
  }

  @Override
  public Map<String, RemoteInfo> apply(ProjectWebhooksResource resource)
      throws NoSuchProjectException {
    Map<String, RemoteInfo> remotes = new HashMap<>();
    Config cfg =
        configFactory.getProjectPluginConfigWithInheritance(resource.getProject(), pluginName);
    for (String name : cfg.getSubsections(REMOTE)) {
      remotes.put(name, fromRemoteConfig(remoteFactory.create(cfg, name)));
    }
    return remotes;
  }
}
