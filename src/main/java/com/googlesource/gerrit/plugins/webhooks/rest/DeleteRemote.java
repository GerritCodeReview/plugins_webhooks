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

import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.Response;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.server.git.meta.MetaDataUpdate;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlesource.gerrit.plugins.webhooks.WebhooksConfig;
import com.googlesource.gerrit.plugins.webhooks.rest.DeleteRemote.Input;
import java.io.IOException;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

public class DeleteRemote implements RestModifyView<RemoteResource, Input> {
  public static class Input {}

  private final Provider<WebhooksConfig> webhooksConfig;
  private final Provider<MetaDataUpdate.User> metaDataUpdateFactory;
  private final ProjectCache projectCache;
  private final Permissions permissions;

  @Inject
  DeleteRemote(
      Provider<WebhooksConfig> webhooksConfig,
      Provider<MetaDataUpdate.User> metaDataUpdateFactory,
      ProjectCache projectCache,
      Permissions permissions) {
    this.metaDataUpdateFactory = metaDataUpdateFactory;
    this.webhooksConfig = webhooksConfig;
    this.projectCache = projectCache;
    this.permissions = permissions;
  }

  @Override
  public Object apply(RemoteResource rsrc, Input input)
      throws RepositoryNotFoundException, IOException, ConfigInvalidException, AuthException {
    if (!permissions.canUpdate(rsrc.getProject())) {
      throw new AuthException("not allowed to update webhooks");
    }

    try (MetaDataUpdate md = metaDataUpdateFactory.get().create(rsrc.getProject())) {
      WebhooksConfig wh = webhooksConfig.get();
      wh.load(md);
      wh.deleteRemote(rsrc.getRemoteConfig().getName());
      wh.commit(md);
    }

    projectCache.evict(rsrc.getProject());

    return Response.none();
  }
}
