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
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlesource.gerrit.plugins.webhooks.Configuration;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitTransformer {
  private static final Logger log = LoggerFactory.getLogger(CommitTransformer.class);

  private final Provider<Identity.Builder> provider;
  private final DateTimeTransformer toDateTime;
  private final CommitUrlProvider toUrl;
  private final int pathsNo;

  @Inject
  protected CommitTransformer(
      Provider<Identity.Builder> provider,
      DateTimeTransformer toDateTime,
      CommitUrlProvider toUrl,
      Configuration cfg) {
    this.provider = provider;
    this.toDateTime = toDateTime;
    this.toUrl = toUrl;
    this.pathsNo = cfg.getGitHubPushPathsNo();
  }

  public Optional<Commit> apply(
      Repository repo, Project.NameKey name, CommitsTransformer.CommitInfo input) {
    try (RevWalk w = new RevWalk(repo);
        DiffFormatter f = new DiffFormatter(NullOutputStream.INSTANCE);
        ObjectReader r = repo.newObjectReader()) {
      RevCommit commit = w.parseCommit(input.obj);
      Commit.Builder builder =
          new Commit.Builder()
              .withId(commit.getId().getName())
              .withTreeId(commit.getTree().getId().getName())
              .withDistinct(input.distinct)
              .withMessage(commit.getFullMessage())
              .withTimestamp(toDateTime.apply(commit.getCommitterIdent()))
              .withUrl(toUrl.get(name, commit))
              .withAuthor(provider.get().withAccount(commit.getAuthorIdent()).build())
              .withCommitter(provider.get().withAccount(commit.getCommitterIdent()).build());

      RevCommit p =
          commit.getParentCount() == 0 ? null : w.parseCommit(commit.getParent(0).getId());
      f.setRepository(repo);
      f.setDetectRenames(true);
      AbstractTreeIterator oldTree =
          p == null ? new EmptyTreeIterator() : new CanonicalTreeParser(null, r, p.getTree());
      AbstractTreeIterator newTree = new CanonicalTreeParser(null, r, commit.getTree());
      List<DiffEntry> diffs = f.scan(oldTree, newTree);
      List<String> added = new LinkedList<>();
      List<String> removed = new LinkedList<>();
      List<String> modified = new LinkedList<>();
      int paths = 0;
      for (DiffEntry diff : diffs) {
        if (diff.getChangeType() == ChangeType.DELETE
            || diff.getChangeType() == ChangeType.RENAME) {
          removed.add(diff.getOldPath());
        }
        if (diff.getChangeType() == ChangeType.ADD
            || diff.getChangeType() == ChangeType.COPY
            || diff.getChangeType() == ChangeType.RENAME) {
          added.add(diff.getNewPath());
        } else if (diff.getChangeType().equals(ChangeType.MODIFY)) {
          modified.add(diff.getNewPath());
        }
        if (pathsNo == ++paths) {
          log.info(
              "Paths limit [{}] was reached for commit {} in repo {}",
              pathsNo,
              input.obj.getId().getName(),
              name);
          break;
        }
      }
      builder.withAdded(added).withRemoved(removed).withModified(modified);

      return Optional.of(builder.build());
    } catch (IOException e) {
      log.info("Parsing commit data {} in repo {} failed", input.obj.getId().getName(), name, e);
      return Optional.empty();
    }
  }
}
