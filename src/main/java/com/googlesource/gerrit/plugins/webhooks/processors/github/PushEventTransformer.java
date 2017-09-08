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

import com.google.common.collect.Streams;
import com.google.gerrit.server.data.RefUpdateAttribute;
import com.google.gerrit.server.events.RefUpdatedEvent;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlesource.gerrit.plugins.webhooks.processors.github.CommitsTransformer.CommitInfo;
import com.googlesource.gerrit.plugins.webhooks.processors.github.OperationTransformer.Operation;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushEventTransformer implements Function<RefUpdatedEvent, Optional<PushEvent>> {
  private static final Logger log = LoggerFactory.getLogger(PushEventTransformer.class);

  private final GitRepositoryManager repositoryMgr;
  private final OperationTransformer toOperation;
  private final CommitsTransformer toCommitInfos;
  private final RepositoryTransformer toRepository;
  private final Provider<Identity.Builder> provider;
  private final CommitTransformer toCommit;

  @Inject
  protected PushEventTransformer(
      GitRepositoryManager repositoryMgr,
      OperationTransformer toOperation,
      RepositoryTransformer toRepository,
      CommitsTransformer toCommitInfos,
      Provider<Identity.Builder> provider,
      CommitTransformer toCommit) {
    this.repositoryMgr = repositoryMgr;
    this.toOperation = toOperation;
    this.toRepository = toRepository;
    this.toCommitInfos = toCommitInfos;
    this.provider = provider;
    this.toCommit = toCommit;
  }

  @Override
  public Optional<PushEvent> apply(RefUpdatedEvent event) {
    try (Repository repo = repositoryMgr.openRepository(event.getProjectNameKey())) {
      RefUpdateAttribute update = event.refUpdate.get();
      Operation o = toOperation.get(repo, update.oldRev, update.newRev);
      PushEvent.Builder builder =
          new PushEvent.Builder()
              .withRef(event.getRefName())
              .withBefore(update.oldRev)
              .withAfter(update.newRev)
              .withOperation(o)
              .withRepository(toRepository.apply(event.getProjectNameKey()))
              .withPusher(provider.get().withAccount(event.submitter.get()).build());

      if (!o.deleted) {
        List<CommitInfo> infos = toCommitInfos.get(repo, event.getRefName(), update, o.created);
        if (!infos.isEmpty()) {
          List<Commit> commits = toCommits(event, repo, infos);
          if (!commits.isEmpty()) {
            Commit head = commits.get(0);
            builder.withHeadCommit(head);
            builder.withCommits(reverse(commits));
          }
        }
      }

      return Optional.of(builder.build());
    } catch (IOException e) {
      log.warn("Processing repository {} failed", event.getProjectNameKey().get(), e);
    }
    return Optional.empty();
  }

  private List<Commit> toCommits(RefUpdatedEvent event, Repository repo, List<CommitInfo> infos) {
    return infos
        .stream()
        .map(
            input -> {
              return toCommit.apply(repo, event.getProjectNameKey(), input);
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private List<Commit> reverse(List<Commit> commits) {
    return Streams.stream(
            commits.stream().collect(Collectors.toCollection(ArrayDeque::new)).descendingIterator())
        .collect(Collectors.toList());
  }
}
