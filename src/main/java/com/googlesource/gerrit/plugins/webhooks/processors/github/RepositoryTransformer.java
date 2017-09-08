// Copyright (C) 2017 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.webhooks.processors.github;

import static com.google.gerrit.extensions.restapi.Url.encode;

import com.google.common.base.Strings;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryTransformer implements Function<Project.NameKey, Repository> {
  private static final Logger log = LoggerFactory.getLogger(RepositoryTransformer.class);

  protected final ProjectCache projects;
  private final String url;

  @Inject
  protected RepositoryTransformer(ProjectCache projects, @CanonicalWebUrl @Nullable String url) {
    this.projects = projects;
    this.url = url;
  }

  @Override
  public Repository apply(Project.NameKey name) {
    ProjectState state = projects.get(name);
    if (state == null) {
      log.warn("Project {} doesn't exist", name);
      return null;
    }

    Project project = state.getProject();
    return new Repository.Builder()
        .withId(project.getName())
        .withName(project.getName())
        .withFullName(project.getName())
        .withDescription(project.getDescription())
        .withUrl(
            !Strings.isNullOrEmpty(url)
                ? String.format("%s#/admin/projects/%s", url, encode(name.get()))
                : null)
        .build();
  }
}
