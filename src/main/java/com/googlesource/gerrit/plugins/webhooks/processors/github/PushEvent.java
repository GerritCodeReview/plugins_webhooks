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

import com.googlesource.gerrit.plugins.webhooks.processors.github.OperationTransformer.Operation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PushEvent implements GitHubEvent {
  public static class Builder {
    private String ref;
    private String before;
    private String after;
    private boolean created;
    private boolean deleted;
    private boolean forced;
    private String baseRef;
    private List<Commit> commits;
    private Commit headCommit;
    private Repository repository;
    private Identity pusher;

    public PushEvent build() {
      return new PushEvent(
          ref,
          before,
          after,
          created,
          deleted,
          forced,
          baseRef,
          Optional.ofNullable(commits).orElse(Collections.emptyList()),
          headCommit,
          repository,
          pusher);
    }

    public Builder withRef(String ref) {
      this.ref = ref;
      return this;
    }

    public Builder withBefore(String before) {
      this.before = before;
      return this;
    }

    public Builder withAfter(String after) {
      this.after = after;
      return this;
    }

    public Builder withOperation(Operation operation) {
      this.created = operation.created;
      this.deleted = operation.deleted;
      this.forced = operation.forced;
      return this;
    }

    public Builder withCommits(List<Commit> commits) {
      this.commits = commits;
      return this;
    }

    public Builder withHeadCommit(Commit headCommit) {
      this.headCommit = headCommit;
      return this;
    }

    public Builder withBaseRef(String baseRef) {
      this.baseRef = baseRef;
      return this;
    }

    public Builder withRepository(Repository repository) {
      this.repository = repository;
      return this;
    }

    public Builder withPusher(Identity pusher) {
      this.pusher = pusher;
      return this;
    }
  }

  public final String ref;
  public final String before;
  public final String after;
  public final boolean created;
  public final boolean deleted;
  public final boolean forced;
  public final String baseRef;
  public final List<Commit> commits;
  public final Commit headCommit;
  public final Repository repository;
  public final Identity pusher;

  protected PushEvent(
      String ref,
      String before,
      String after,
      boolean created,
      boolean deleted,
      boolean forced,
      String baseRef,
      List<Commit> commits,
      Commit headCommit,
      Repository repository,
      Identity pusher) {
    this.ref = ref;
    this.before = before;
    this.after = after;
    this.created = created;
    this.deleted = deleted;
    this.forced = forced;
    this.baseRef = baseRef;
    this.commits = commits;
    this.headCommit = headCommit;
    this.repository = repository;
    this.pusher = pusher;
  }

  @Override
  public String type() {
    return "push";
  }
}
