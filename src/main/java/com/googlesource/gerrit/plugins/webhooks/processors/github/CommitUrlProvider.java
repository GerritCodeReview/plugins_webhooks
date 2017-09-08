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

import com.google.gerrit.reviewdb.client.Project;
import org.eclipse.jgit.revwalk.RevCommit;

public class CommitUrlProvider {
  @SuppressWarnings("unused")
  public String get(Project.NameKey name, RevCommit commit) {
    // by default there is no commit url
    return null;
  }
}
