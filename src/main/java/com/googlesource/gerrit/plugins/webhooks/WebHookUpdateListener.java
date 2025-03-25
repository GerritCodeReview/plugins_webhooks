// Copyright (C) 2025 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.webhooks;

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.entities.Project;
import com.google.gerrit.entities.RefNames;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class WebHookUpdateListener implements GitReferenceUpdatedListener {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private final String cfgFileName;
  private final GitRepositoryManager repoManager;

  @Inject
  WebHookUpdateListener(
      @WebhooksConfigFileName String cfgFileName, GitRepositoryManager repoManager) {
    this.cfgFileName = cfgFileName;
    this.repoManager = repoManager;
  }

  @Override
  public void onGitReferenceUpdated(Event event) {
    if (event.getRefName().equals(RefNames.REFS_CONFIG)) {
      Project.NameKey p = Project.NameKey.parse(event.getProjectName());
      try (Repository repo = repoManager.openRepository(p);
          RevWalk rw = new RevWalk(repo)) {
        RevCommit commit = rw.parseCommit(ObjectId.fromString(event.getNewObjectId()));
        RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter diffFormatter = new DiffFormatter(out);
        diffFormatter.setRepository(repo);
        List<DiffEntry> diffs = diffFormatter.scan(parent.getTree(), commit.getTree());
        for (DiffEntry diff : diffs) {
          if (diff.getNewPath().equals(cfgFileName)) {
            log.atInfo().log("Webhook created/updated for project %s", p.get());
            return;
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
