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

import org.junit.Test;

import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.Project.NameKey;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gerrit.server.events.RefUpdatedEvent;

public class GerritEventProcessorTest {
  private static final String PROJECT = "p";
  private static final Project.NameKey PROJECT_NAME = new Project.NameKey(PROJECT);

  private static final ProjectCreatedEvent PROJECT_CREATED =
      new ProjectCreatedEvent() {
        @Override
        public NameKey getProjectNameKey() {
          return PROJECT_NAME;
        }
      };

  private static final RefUpdatedEvent REF_UPDATED =
      new RefUpdatedEvent() {
        @Override
        public NameKey getProjectNameKey() {
          return PROJECT_NAME;
        }
      };

  @Test
  public void eventsNotSpecifiedAllEventsShouldPost() throws Exception {
    GerritEventProcessor objectUnderTest = new GerritEventProcessor(PROJECT_CREATED);
    boolean actual = objectUnderTest.shouldPost(new String[] {});
    assertThat(actual).isTrue();

    objectUnderTest = new GerritEventProcessor(REF_UPDATED);
    actual = objectUnderTest.shouldPost(new String[] {});
    assertThat(actual).isTrue();
  }

  @Test
  public void specifiedEventTypesShouldPost() throws Exception {
    GerritEventProcessor objectUnderTest = new GerritEventProcessor(PROJECT_CREATED);
    boolean actual = objectUnderTest.shouldPost(new String[] {"project-created"});
    assertThat(actual).isTrue();
  }

  @Test
  public void nonSpecifiedProjectEventTypesNotPosted() throws Exception {
    GerritEventProcessor objectUnderTest = new GerritEventProcessor(REF_UPDATED);
    boolean actual = objectUnderTest.shouldPost(new String[] {"project-created"});
    assertThat(actual).isFalse();
  }
}
