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
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.MetaDataUpdate;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import com.googlesource.gerrit.plugins.webhooks.WebhooksConfig;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Config;

@Singleton
public class UpsertWebhook {
  public static class Input {
    public String url;
    public List<String> events;
    public Integer connectionTimeout;
    public Integer socketTimeout;
    public Integer maxTries;
    public Integer retryInterval;
    public Boolean sslVerify;
  }

  public static class Updater implements RestModifyView<RemoteResource, UpsertWebhook.Input> {
    private final UpsertWebhook upserter;

    @Inject
    Updater(UpsertWebhook upserter) {
      this.upserter = upserter;
    }

    @Override
    public RemoteInfo apply(RemoteResource rsrc, UpsertWebhook.Input in)
        throws ConfigInvalidException, RepositoryNotFoundException, IOException,
            NoSuchProjectException {
      return upserter.upsert(rsrc.getProject(), rsrc.getRemoteConfig().getName(), in);
    }
  }

  public static class Inserter
      implements RestModifyView<ProjectWebhooksResource, UpsertWebhook.Input> {
    public interface Factory {
      Inserter create(String remoteName);
    }

    private final UpsertWebhook upserter;
    private final String remoteName;

    @AssistedInject
    Inserter(UpsertWebhook upserter, @Assisted String remoteName) {
      this.upserter = upserter;
      this.remoteName = remoteName;
    }

    @Override
    public RemoteInfo apply(ProjectWebhooksResource rsrc, UpsertWebhook.Input in)
        throws RepositoryNotFoundException, IOException, ConfigInvalidException,
            NoSuchProjectException {
      return upserter.upsert(rsrc.getProject(), remoteName, in);
    }
  }

  private final Provider<WebhooksConfig> webhooksConfig;
  private final Provider<MetaDataUpdate.User> metaDataUpdateFactory;
  private final PluginConfigFactory configFactory;
  private final RemoteConfig.Factory remoteConfigFactory;
  private final String pluginName;
  private final ProjectCache projectCache;

  @Inject
  UpsertWebhook(
      Provider<WebhooksConfig> webhooksConfig,
      Provider<MetaDataUpdate.User> metaDataUpdateFactory,
      PluginConfigFactory configFactory,
      RemoteConfig.Factory remoteConfigFactory,
      @PluginName String pluginName,
      ProjectCache projectCache) {
    this.webhooksConfig = webhooksConfig;
    this.metaDataUpdateFactory = metaDataUpdateFactory;
    this.configFactory = configFactory;
    this.remoteConfigFactory = remoteConfigFactory;
    this.pluginName = pluginName;
    this.projectCache = projectCache;
  }

  public RemoteInfo upsert(Project.NameKey project, String remoteName, Input in)
      throws NoSuchProjectException, ConfigInvalidException, IOException {
    if (Strings.isNullOrEmpty(in.url)) {
      throw new ConfigInvalidException("url must be specificed");
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
