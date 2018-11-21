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

import static com.googlesource.gerrit.plugins.webhooks.rest.GetRemote.fromRemoteConfig;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.RestCollectionCreateView;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.meta.MetaDataUpdate;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import com.googlesource.gerrit.plugins.webhooks.WebhooksConfig;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Config;

@Singleton
public class UpsertRemote {
  public static class Input {
    public String url;
    public List<String> events;
    public Integer connectionTimeout;
    public Integer socketTimeout;
    public Integer maxTries;
    public Integer retryInterval;
    public Boolean sslVerify;
  }

  public static class Updater implements RestModifyView<RemoteResource, UpsertRemote.Input> {
    private final UpsertRemote upserter;

    @Inject
    Updater(UpsertRemote upserter) {
      this.upserter = upserter;
    }

    @Override
    public RemoteInfo apply(RemoteResource rsrc, UpsertRemote.Input in)
        throws ConfigInvalidException, RepositoryNotFoundException, IOException,
            NoSuchProjectException, AuthException {
      return upserter.upsert(rsrc.getProject(), rsrc.getRemoteConfig().getName(), in);
    }
  }

  public static class Inserter
      implements RestCollectionCreateView<
          ProjectWebhooksResource, RemoteResource, UpsertRemote.Input> {

    private final UpsertRemote upserter;

    @Inject
    Inserter(UpsertRemote upserter) {
      this.upserter = upserter;
    }

    @Override
    public RemoteInfo apply(
        ProjectWebhooksResource rsrc, IdString remoteName, UpsertRemote.Input in)
        throws RepositoryNotFoundException, IOException, ConfigInvalidException,
            NoSuchProjectException, AuthException {
      return upserter.upsert(rsrc.getProject(), remoteName.get(), in);
    }
  }

  private final Provider<WebhooksConfig> webhooksConfig;
  private final Provider<MetaDataUpdate.User> metaDataUpdateFactory;
  private final PluginConfigFactory configFactory;
  private final RemoteConfig.Factory remoteConfigFactory;
  private final String pluginName;
  private final ProjectCache projectCache;
  private final Permissions permissions;

  @Inject
  UpsertRemote(
      Provider<WebhooksConfig> webhooksConfig,
      Provider<MetaDataUpdate.User> metaDataUpdateFactory,
      PluginConfigFactory configFactory,
      RemoteConfig.Factory remoteConfigFactory,
      @PluginName String pluginName,
      ProjectCache projectCache,
      Permissions permissions) {
    this.webhooksConfig = webhooksConfig;
    this.metaDataUpdateFactory = metaDataUpdateFactory;
    this.configFactory = configFactory;
    this.remoteConfigFactory = remoteConfigFactory;
    this.pluginName = pluginName;
    this.projectCache = projectCache;
    this.permissions = permissions;
  }

  public RemoteInfo upsert(Project.NameKey project, String remoteName, Input in)
      throws NoSuchProjectException, ConfigInvalidException, IOException, AuthException {
    if (Strings.isNullOrEmpty(in.url)) {
      throw new ConfigInvalidException("url must be specificed");
    }
    if (!permissions.canUpdate(project)) {
      throw new AuthException("not allowed to update webhooks");
    }

    try (MetaDataUpdate md = metaDataUpdateFactory.get().create(project)) {
      WebhooksConfig wh = webhooksConfig.get();
      wh.load(md);
      wh.upsertRemote(remoteName, in);
      wh.commit(md);
    }

    projectCache.evict(project);

    Config cfg = configFactory.getProjectPluginConfigWithInheritance(project, pluginName);
    RemoteConfig rc = remoteConfigFactory.create(cfg, remoteName);
    return fromRemoteConfig(rc);
  }
}
