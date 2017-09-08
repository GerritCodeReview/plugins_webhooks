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

import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.data.RefUpdateAttribute;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.webhooks.Configuration;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitsTransformer {
  public static class CommitInfo {
    public final RevCommit obj;
    public final boolean distinct;

    CommitInfo(RevCommit obj, boolean distinct) {
      this.obj = obj;
      this.distinct = distinct;
    }
  }

  private static final Logger log = LoggerFactory.getLogger(CommitsTransformer.class);
  private final Integer commitsNo;

  @Inject
  CommitsTransformer(Configuration cfg) {
    this.commitsNo = cfg.getGitHubPushCommitsNo();
  }

  public List<CommitInfo> get(
      Repository repo, String ref, RefUpdateAttribute update, boolean onlyDistinct) {
    String before = update.oldRev;
    String after = update.newRev;

    List<String> heads = heads(repo, ref);
    try (RevWalk w = new RevWalk(repo)) {
      w.setRetainBody(false);
      RevCommit head = w.parseCommit(repo.resolve(after));

      w.reset();
      w.markStart(head);
      if (!onlyDistinct) {
        w.markUninteresting(w.parseCommit(repo.resolve(before)));
      }

      List<CommitInfo> commits = new LinkedList<>();
      boolean lastDistinct = isDistinct(repo, heads, head.getId().getName());
      commits.add(new CommitInfo(head, lastDistinct));

      if (lastDistinct || !onlyDistinct) {
        //skip head
        w.next();
        for (RevCommit rev : w) {
          boolean distinct = lastDistinct && isDistinct(repo, heads, rev.getId().getName());
          if (!distinct && onlyDistinct) {
            break;
          }
          commits.add(new CommitInfo(rev, distinct));
          if (commits.size() >= commitsNo) {
            log.info(
                "Limit of push commits [{}] was reached for ref {}, oldRev {}, newRev {}",
                new Object[] {commitsNo, ref, before, after});
            break;
          }
          lastDistinct = distinct;
        }
      }
      return commits;
    } catch (RevisionSyntaxException | IOException e) {
      log.warn(
          "Getting list of commits for ref {}, oldRev {}, newRev {} failed", ref, before, after, e);
      return Collections.emptyList();
    }
  }

  private boolean isDistinct(Repository repo, List<String> heads, String rev) {
    try (RevWalk w = new RevWalk(repo)) {
      w.setRetainBody(false);
      return !heads.contains(rev)
          && heads
              .stream()
              .noneMatch(
                  tip -> {
                    try {
                      w.reset();
                      RevCommit parsedTip = w.parseCommit(repo.resolve(tip));
                      RevCommit parsedRev = w.parseCommit(repo.resolve(rev));
                      return w.isMergedInto(parsedRev, parsedTip);
                    } catch (IOException e) {
                      return true;
                    }
                  });
    }
  }

  private List<String> heads(Repository repo, String ref) {
    Map<String, Ref> allRefs = repo.getAllRefs();
    try (RevWalk w = new RevWalk(repo)) {
      w.setRetainBody(false);
      return allRefs
          .entrySet()
          .stream()
          .filter(
              refEntry -> {
                String name = refEntry.getKey();
                return !name.equals(ref)
                    && !RefNames.HEAD.equals(name)
                    && !name.startsWith(RefNames.REFS_CHANGES)
                    && !name.startsWith(RefNames.REFS_CACHE_AUTOMERGE);
              })
          .map(
              refEntry -> {
                w.reset();
                try {
                  return Optional.<String>of(
                      w.parseCommit(refEntry.getValue().getObjectId()).getId().getName());
                } catch (IOException e) {
                  return Optional.<String>empty();
                }
              })
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
    }
  }
}
