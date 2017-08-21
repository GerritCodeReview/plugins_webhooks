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

package com.googlesource.gerrit.plugins.webhooks;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.Project.NameKey;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gerrit.server.events.RefUpdatedEvent;
import com.google.gerrit.server.project.NoSuchProjectException;
import org.eclipse.jgit.lib.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {

  private static final String PROJECT = "p";
  private static final Project.NameKey PROJECT_NAME = new Project.NameKey(PROJECT);
  private static final String PLUGIN = "webhooks";
  private static final String REMOTE = "remote";
  private static final String FOO = "foo";
  private static final String URL = "url";
  private static final String FOO_URL = "foo-url";
  private static final String EVENT = "event";

  private static final ProjectCreatedEvent PROJECT_CREATED =
      new ProjectCreatedEvent() {
        public NameKey getProjectNameKey() {
          return PROJECT_NAME;
        }
      };

  private static final RefUpdatedEvent REF_UPDATED =
      new RefUpdatedEvent() {
        public NameKey getProjectNameKey() {
          return PROJECT_NAME;
        }
      };

  @Mock private PluginConfigFactory configFactory;

  @Mock private PostTask.Factory taskFactory;

  @Mock private PostTask postTask;

  private Config config = new Config();

  private EventHandler eventHandler;

  @Before
  public void setup() throws NoSuchProjectException {
    when(configFactory.getProjectPluginConfigWithInheritance(PROJECT_NAME, PLUGIN))
        .thenReturn(config);
    when(taskFactory.create(anyString(), anyString())).thenReturn(postTask);
    eventHandler = new EventHandler(configFactory, PLUGIN, taskFactory);
  }

  @Test
  public void remoteUrlUndefinedEventsNotPosted() {
    eventHandler.onEvent(PROJECT_CREATED);
    verifyZeroInteractions(postTask);
  }

  @Test
  public void eventTypesNotSpecifiedAllEventsPosted() {
    config.setString(REMOTE, FOO, URL, FOO_URL);

    eventHandler.onEvent(PROJECT_CREATED);
    eventHandler.onEvent(REF_UPDATED);
    verify(postTask, times(2)).schedule();
  }

  @Test
  public void specifiedEventTypesPosted() {
    config.setString(REMOTE, FOO, URL, FOO_URL);
    config.setString(REMOTE, FOO, EVENT, "project-created");

    eventHandler.onEvent(PROJECT_CREATED);
    verify(postTask, times(1)).schedule();
  }

  @Test
  public void nonSpecifiedProjectEventTypesNotPosted() {
    config.setString(REMOTE, FOO, URL, FOO_URL);
    config.setString(REMOTE, FOO, EVENT, "project-created");

    eventHandler.onEvent(REF_UPDATED);
    verifyZeroInteractions(postTask);
  }

  @Test
  public void nonProjectEventNotPosted() {
    config.setString(REMOTE, FOO, URL, FOO_URL);

    Event nonProjectEvent = new Event("non-project-event") {};
    eventHandler.onEvent(nonProjectEvent);
    verifyZeroInteractions(postTask);
  }
}
