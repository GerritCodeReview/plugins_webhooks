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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Commit {
  public static class Builder {
    private String id;
    private String treeId;
    private boolean distinct;
    private String message;
    private String timestamp;
    private String url;
    private Identity author;
    private Identity committer;
    private List<String> added;
    private List<String> removed;
    private List<String> modified;

    public Commit build() {
      return new Commit(
          id,
          treeId,
          distinct,
          message,
          timestamp,
          url,
          author,
          committer,
          Optional.ofNullable(added).orElse(Collections.emptyList()),
          Optional.ofNullable(removed).orElse(Collections.emptyList()),
          Optional.ofNullable(modified).orElse(Collections.emptyList()));
    }

    public Builder withId(String id) {
      this.id = id;
      return this;
    }

    public Builder withTreeId(String treeId) {
      this.treeId = treeId;
      return this;
    }

    public Builder withDistinct(boolean distinct) {
      this.distinct = distinct;
      return this;
    }

    public Builder withMessage(String message) {
      this.message = message;
      return this;
    }

    public Builder withTimestamp(String timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder withUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder withAuthor(Identity author) {
      this.author = author;
      return this;
    }

    public Builder withCommitter(Identity committer) {
      this.committer = committer;
      return this;
    }

    public Builder withAdded(List<String> added) {
      this.added = added;
      return this;
    }

    public Builder withRemoved(List<String> removed) {
      this.removed = removed;
      return this;
    }

    public Builder withModified(List<String> modified) {
      this.modified = modified;
      return this;
    }
  }

  public final String id;
  public final String treeId;
  public final boolean distinct;
  public final String message;
  public final String timestamp;
  public final String url;
  public final Identity author;
  public final Identity committer;
  public final List<String> added;
  public final List<String> removed;
  public final List<String> modified;

  private Commit(
      String id,
      String treeId,
      boolean distinct,
      String message,
      String timestamp,
      String url,
      Identity author,
      Identity committer,
      List<String> added,
      List<String> removed,
      List<String> modified) {
    this.id = id;
    this.treeId = treeId;
    this.distinct = distinct;
    this.message = message;
    this.timestamp = timestamp;
    this.url = url;
    this.author = author;
    this.committer = committer;
    this.added = added;
    this.removed = removed;
    this.modified = modified;
  }
}
