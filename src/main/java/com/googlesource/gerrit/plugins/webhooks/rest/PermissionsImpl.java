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

import static com.google.gerrit.reviewdb.client.RefNames.REFS_CONFIG;

import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PermissionsImpl implements Permissions {

  private final ProjectCache projectCache;
  private final Provider<CurrentUser> currentUser;

  @Inject
  PermissionsImpl(ProjectCache projectCache, Provider<CurrentUser> currentUser) {
    this.projectCache = projectCache;
    this.currentUser = currentUser;
  }

  @Override
  public boolean canRead(Project.NameKey project) {
    return projectCache
        .get(project)
        .controlFor(currentUser.get())
        .controlForRef(REFS_CONFIG)
        .isVisible();
  }

  @Override
  public boolean canUpdate(Project.NameKey project) {
    return projectCache
        .get(project)
        .controlFor(currentUser.get())
        .controlForRef(REFS_CONFIG)
        .canUpdate();
  }
}
