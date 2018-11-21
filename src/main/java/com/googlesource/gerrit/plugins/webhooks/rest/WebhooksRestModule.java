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

import static com.google.gerrit.server.config.ConfigResource.CONFIG_KIND;
import static com.googlesource.gerrit.plugins.webhooks.rest.ProjectWebhooksResource.PROJECT_WEBHOOK_KIND;
import static com.googlesource.gerrit.plugins.webhooks.rest.RemoteResource.REMOTE_KIND;

import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.extensions.restapi.RestApiModule;

public class WebhooksRestModule extends RestApiModule {

  @Override
  protected void configure() {
    DynamicMap.mapOf(binder(), PROJECT_WEBHOOK_KIND);
    DynamicMap.mapOf(binder(), REMOTE_KIND);

    child(CONFIG_KIND, "projects").to(ProjectsCollection.class);
    child(PROJECT_WEBHOOK_KIND, "remotes").to(RemotesCollection.class);
    create(REMOTE_KIND).to(UpsertRemote.Inserter.class);
    get(REMOTE_KIND).to(GetRemote.class);
    put(REMOTE_KIND).to(UpsertRemote.Updater.class);
    delete(REMOTE_KIND).to(DeleteRemote.class);

    bind(Permissions.class).to(PermissionsImpl.class);
  }
}
