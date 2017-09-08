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

import java.io.IOException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationTransformer {
  public static class Operation {
    public final boolean created;
    public final boolean deleted;
    public final boolean forced;

    private Operation() {
      this(false, false, false);
    }

    private Operation(boolean created, boolean deleted, boolean forced) {
      this.created = created;
      this.deleted = deleted;
      this.forced = forced;
    }
  }

  private static final Logger log = LoggerFactory.getLogger(OperationTransformer.class);
  private static final Operation CREATED = new Operation(true, false, false);
  private static final Operation DELETED = new Operation(false, true, false);
  private static final Operation FORCED = new Operation(false, false, true);
  private static final Operation UPDATED = new Operation();

  public Operation get(Repository repo, String before, String after) {
    if (ObjectId.zeroId().getName().equals(before)) {
      return CREATED;
    }

    if (ObjectId.zeroId().getName().equals(after)) {
      return DELETED;
    }

    if (before.equals(after)) {
      return UPDATED;
    }

    try (RevWalk walk = new RevWalk(repo)) {
      walk.setRetainBody(false);
      RevObject oldRev = walk.parseAny(ObjectId.fromString(before));
      RevObject newRev = walk.parseAny(ObjectId.fromString(after));
      if (!(oldRev instanceof RevCommit)
          || !(newRev instanceof RevCommit)
          || !walk.isMergedInto((RevCommit) oldRev, (RevCommit) newRev)) {
        return FORCED;
      }
      return UPDATED;
    } catch (IOException e) {
      log.error(
          String.format(
              "Exception was thrown while oldRev: %s and newRev: %s were parsed", before, after),
          e);
      return FORCED;
    }
  }
}
