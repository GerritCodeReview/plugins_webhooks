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

public class Repository {
  public static class Builder {
    private String id;
    private String name;
    private String fullName;
    private String description;
    private String url;

    public Repository build() {
      return new Repository(id, name, fullName, description, url);
    }

    public Builder withId(String id) {
      this.id = id;
      return this;
    }

    public Builder withFullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder withUrl(String url) {
      this.url = url;
      return this;
    }
  }

  public final String id;
  public final String name;
  public final String fullName;
  public final String description;
  public final String url;

  protected Repository(String id, String name, String fullName, String description, String url) {
    this.id = id;
    this.name = name;
    this.fullName = fullName;
    this.description = description;
    this.url = url;
  }
}
