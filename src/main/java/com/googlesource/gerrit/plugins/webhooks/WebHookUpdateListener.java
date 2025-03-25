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
import com.google.gerrit.server.patch.DiffNotAvailableException;
import com.google.gerrit.server.patch.DiffOperations;
import com.google.gerrit.server.patch.DiffOptions;
import com.google.gerrit.server.patch.filediff.FileDiffOutput;
import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.jgit.lib.ObjectId;

public class WebHookUpdateListener implements GitReferenceUpdatedListener {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private final String cfgFileName;
  private final DiffOperations diffOperations;

  @Inject
  WebHookUpdateListener(@WebhooksConfigFileName String cfgFileName, DiffOperations diffOperations) {
    this.cfgFileName = cfgFileName;
    this.diffOperations = diffOperations;
  }

  @Override
  public void onGitReferenceUpdated(Event event) {
    if (event.getRefName().equals(RefNames.REFS_CONFIG)) {
      Project.NameKey project = Project.NameKey.parse(event.getProjectName());
      try {
        Map<String, FileDiffOutput> diffByFile =
            diffOperations.listModifiedFilesAgainstParent(
                project,
                ObjectId.fromString(event.getNewObjectId()),
                /* parentNum= */ 0,
                DiffOptions.DEFAULTS);
        if (diffByFile.containsKey(cfgFileName)) {
          FileDiffOutput diff = diffByFile.get(cfgFileName);
          if (diff != null) {
            log.atInfo().log("Webhook config %s for project %s", diff.changeType(), project.get());
          }
        }
      } catch (DiffNotAvailableException e) {
        log.atSevere().withCause(e).log(
            "Unable to get diff for %s in %s", event.getNewObjectId(), event.getProjectName());
      }
    }
  }
}
