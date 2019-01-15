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

package com.googlesource.gerrit.plugins.webhooks.processors;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.events.RefUpdatedEvent;
import com.googlesource.gerrit.plugins.webhooks.EventProcessor;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractEventProcessorTest {
  private static final String PROJECT = "p";
  private static final Project.NameKey PROJECT_NAME = new Project.NameKey(PROJECT);

  private static final ProjectCreatedEvent PROJECT_CREATED =
      new ProjectCreatedEvent() {
        @Override
        public Project.NameKey getProjectNameKey() {
          return PROJECT_NAME;
        }
      };

  private static final RefUpdatedEvent REF_UPDATED =
      new RefUpdatedEvent() {
        @Override
        public Project.NameKey getProjectNameKey() {
          return PROJECT_NAME;
        }
      };

  @Mock private RemoteConfig remote;

  private TestEventProcessor processor;

  @Before
  public void setup() throws Exception {
    processor = new TestEventProcessor();
  }

  @Test
  public void eventsNotSpecifiedAllEventsShouldProcess() throws Exception {
    when(remote.getEvents()).thenReturn(new String[] {});
    boolean actual = processor.shouldProcess(PROJECT_CREATED, remote);
    assertThat(actual).isTrue();

    actual = processor.shouldProcess(REF_UPDATED, remote);
    assertThat(actual).isTrue();
  }

  @Test
  public void specifiedEventTypesShouldProcess() throws Exception {
    when(remote.getEvents()).thenReturn(new String[] {"project-created"});
    boolean actual = processor.shouldProcess(PROJECT_CREATED, remote);
    assertThat(actual).isTrue();
  }

  @Test
  public void nonSpecifiedProjectEventTypesNotProcess() throws Exception {
    when(remote.getEvents()).thenReturn(new String[] {"project-created"});
    boolean actual = processor.shouldProcess(REF_UPDATED, remote);
    assertThat(actual).isFalse();
  }

  private class TestEventProcessor extends AbstractEventProcessor {
    @Override
    public Optional<EventProcessor.Request> doProcess(ProjectEvent event, RemoteConfig config) {
      // do nothing
      return Optional.empty();
    }
  }
}
