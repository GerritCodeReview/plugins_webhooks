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

import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.extensions.restapi.ChildCollection;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.RestView;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.ConfigResource;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProjectsCollection
    implements ChildCollection<ConfigResource, ProjectWebhooksResource> {

  private final DynamicMap<RestView<ProjectWebhooksResource>> views;
  private final ProjectCache projectCache;

  @Inject
  ProjectsCollection(
      DynamicMap<RestView<ProjectWebhooksResource>> views, ProjectCache projectCache) {
    this.views = views;
    this.projectCache = projectCache;
  }

  @Override
  public RestView<ConfigResource> list() {
    throw new NotImplementedException();
  }

  @Override
  public ProjectWebhooksResource parse(ConfigResource parent, IdString id)
      throws ResourceNotFoundException {
    return parse(id.get());
  }

  private ProjectWebhooksResource parse(String id) throws ResourceNotFoundException {
    Project.NameKey n = new Project.NameKey(id);
    if (projectCache.get(n) != null) {
      return new ProjectWebhooksResource(new Project.NameKey(id));
    }
    throw new ResourceNotFoundException(String.format("Project %s does not exist", id));
  }

  @Override
  public DynamicMap<RestView<ProjectWebhooksResource>> views() {
    return views;
  }
}
