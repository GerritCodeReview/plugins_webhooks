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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.ChildCollection;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.RestView;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import org.eclipse.jgit.lib.Config;

@Singleton
public class RemotesCollection implements ChildCollection<ProjectWebhooksResource, RemoteResource> {

  private final DynamicMap<RestView<RemoteResource>> views;
  private final PluginConfigFactory configFactory;
  private final RemoteConfig.Factory remoteFactory;
  private final Provider<ListRemotes> list;
  private final String pluginName;

  @Inject
  RemotesCollection(
      DynamicMap<RestView<RemoteResource>> views,
      PluginConfigFactory configFactory,
      RemoteConfig.Factory remoteFactory,
      Provider<ListRemotes> list,
      @PluginName String pluginName) {
    this.views = views;
    this.configFactory = configFactory;
    this.remoteFactory = remoteFactory;
    this.list = list;
    this.pluginName = pluginName;
  }

  @Override
  public RestView<ProjectWebhooksResource> list() throws ResourceNotFoundException, AuthException {
    return list.get();
  }

  @Override
  public RemoteResource parse(ProjectWebhooksResource parent, IdString id)
      throws ResourceNotFoundException, Exception {
    Config cfg =
        configFactory.getProjectPluginConfigWithInheritance(parent.getProject(), pluginName);
    String name = id.get();
    if (!cfg.getSubsections(REMOTE).contains(name)) {
      throw new ResourceNotFoundException(String.format("Remote %s does not exist", name));
    }
    return new RemoteResource(parent.getProject(), remoteFactory.create(cfg, name));
  }

  @Override
  public DynamicMap<RestView<RemoteResource>> views() {
    return views;
  }
}
