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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.eclipse.jgit.lib.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.Project.NameKey;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.events.RefUpdatedEvent;

@RunWith(MockitoJUnitRunner.class)
public class AbstractEventProcessorTest {
  private static final String PROJECT = "p";
  private static final Project.NameKey PROJECT_NAME = new Project.NameKey(PROJECT);
  private static final String REMOTE = "remote";
  private static final String FOO = "foo";
  private static final String EVENT = "event";

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

  @Mock private Config config;

  @Test
  public void eventsNotSpecifiedAllEventsShouldPost() throws Exception {
    when(config.getStringList(eq(REMOTE), eq(FOO), eq(EVENT))).thenReturn(new String[] {});
    AbstractEventProcessor objectUnderTest = new TestEventProcessor(PROJECT_CREATED, config, FOO);
    boolean actual = objectUnderTest.shouldProcess();
    assertThat(actual).isTrue();

    objectUnderTest = new TestEventProcessor(REF_UPDATED, config, FOO);
    actual = objectUnderTest.shouldProcess();
    assertThat(actual).isTrue();
  }

  @Test
  public void specifiedEventTypesShouldPost() throws Exception {
    when(config.getStringList(eq(REMOTE), eq(FOO), eq(EVENT))).thenReturn(new String[] {"project-created"});
    AbstractEventProcessor objectUnderTest = new TestEventProcessor(PROJECT_CREATED, config, FOO);
    boolean actual = objectUnderTest.shouldProcess();
    assertThat(actual).isTrue();
  }

  @Test
  public void nonSpecifiedProjectEventTypesNotPosted() throws Exception {
    when(config.getStringList(eq(REMOTE), eq(FOO), eq(EVENT))).thenReturn(new String[] {"project-created"});
    AbstractEventProcessor objectUnderTest = new TestEventProcessor(REF_UPDATED, config, FOO);
    boolean actual = objectUnderTest.shouldProcess();
    assertThat(actual).isFalse();
  }

  private class TestEventProcessor extends AbstractEventProcessor {
    protected TestEventProcessor(ProjectEvent event, Config cfg, String name) {
      super(event, cfg, name);
    }

    @Override
    public String process() {
      // do nothing
      return null;
    }
  }
}
