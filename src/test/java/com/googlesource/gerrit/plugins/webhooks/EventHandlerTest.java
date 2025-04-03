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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gerrit.entities.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gerrit.server.project.NoSuchProjectException;
import java.util.regex.Pattern;
import org.eclipse.jgit.lib.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {
  private static final Project.NameKey PROJECT_NAME = Project.nameKey("p");
  private static final String PLUGIN = "webhooks";
  private static final String REMOTE = "remote";
  private static final String FOO = "foo";
  private static final String FOO_URL = "foo-url";

  @Mock private Configuration global;

  @Mock private ProjectCreatedEvent projectCreated;

  @Mock private PluginConfigFactory configFactory;

  @Mock private RemoteConfig.Factory remoteFactory;

  @Mock private PostTask.Factory taskFactory;

  @Mock private PostTask postTask;

  @Mock private RemoteConfig remote;

  @Mock private Config config;

  private EventHandler eventHandler;

  @Before
  public void setup() throws NoSuchProjectException {
    when(projectCreated.getProjectNameKey()).thenReturn(PROJECT_NAME);
    when(configFactory.getProjectPluginConfigWithInheritance(PROJECT_NAME, PLUGIN))
        .thenReturn(config);
    when(remoteFactory.create(eq(config), eq(FOO))).thenReturn(remote);
    when(taskFactory.create(eq(projectCreated), eq(remote))).thenReturn(postTask);
    eventHandler = new EventHandler(global, configFactory, PLUGIN, remoteFactory, taskFactory);
  }

  @Test
  public void remoteUrlUndefinedTaskNotScheduled() {
    when(config.getSubsections(eq(REMOTE))).thenReturn(ImmutableSet.of(FOO));
    eventHandler.onEvent(projectCreated);
    verifyNoInteractions(taskFactory);
    verifyNoInteractions(postTask);
  }

  @Test
  public void remoteUrlDefinedTaskScheduled() {
    when(config.getSubsections(eq(REMOTE))).thenReturn(ImmutableSet.of(FOO));
    when(remote.getUrl()).thenReturn(FOO_URL);

    eventHandler.onEvent(projectCreated);
    verify(taskFactory, times(1)).create(eq(projectCreated), eq(remote));
    verify(postTask, times(1)).schedule();
  }

  @Test
  public void nonProjectEventNotProcessed() {
    Event nonProjectEvent = new Event("non-project-event") {};
    eventHandler.onEvent(nonProjectEvent);
    verifyNoInteractions(remoteFactory);
    verifyNoInteractions(taskFactory);
    verifyNoInteractions(postTask);
  }

  @Test
  public void allowedUrlTaskScheduled() {
    when(config.getSubsections(eq(REMOTE))).thenReturn(ImmutableSet.of(FOO));
    when(global.getAllowedUrlPatterns()).thenReturn(ImmutableList.of(Pattern.compile(FOO_URL)));
    when(remote.getUrl()).thenReturn(FOO_URL);

    eventHandler.onEvent(projectCreated);
    verify(taskFactory, times(1)).create(eq(projectCreated), eq(remote));
    verify(postTask, times(1)).schedule();
  }

  @Test
  public void allowedUrlTaskNotScheduled() {
    when(config.getSubsections(eq(REMOTE))).thenReturn(ImmutableSet.of(FOO));
    when(global.getAllowedUrlPatterns())
        .thenReturn(ImmutableList.of(Pattern.compile(FOO_URL + ".*BAR")));
    when(remote.getUrl()).thenReturn(FOO_URL);

    eventHandler.onEvent(projectCreated);
    verifyNoInteractions(taskFactory);
    verifyNoInteractions(postTask);
  }
}
