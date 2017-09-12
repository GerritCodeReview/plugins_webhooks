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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.Project.NameKey;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gerrit.server.events.ProjectEvent;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.googlesource.gerrit.plugins.webhooks.processors.Factories;
import java.util.Optional;
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
  private static final String[] EMPTY_EVENTS_LIST = new String[] {};

  private static final ProjectCreatedEvent PROJECT_CREATED =
      new ProjectCreatedEvent() {
        @Override
        public NameKey getProjectNameKey() {
          return PROJECT_NAME;
        }
      };

  @Mock private PluginConfigFactory configFactory;

  @Mock private PostTask.Factory taskFactory;

  @Mock private PostTask postTask;

  @Mock private Factories provider;

  @Mock private EventProcessor.Factory factory;

  @Mock private EventProcessor processor;

  @Mock private Config config;

  private EventHandler eventHandler;

  @Before
  public void setup() throws NoSuchProjectException {
    when(configFactory.getProjectPluginConfigWithInheritance(PROJECT_NAME, PLUGIN))
        .thenReturn(config);
    when(provider.getFactory(eq(config), eq(FOO))).thenReturn(Optional.of(factory));
    when(factory.create(any(ProjectEvent.class))).thenReturn(processor);
    when(taskFactory.create(anyString(), eq(processor))).thenReturn(postTask);
    eventHandler = new EventHandler(configFactory, PLUGIN, taskFactory, provider);
  }

  @Test
  public void remoteUrlUndefinedEventsNotPosted() {
    eventHandler.onEvent(PROJECT_CREATED);
    verifyZeroInteractions(processor);
    verifyZeroInteractions(postTask);
  }

  @Test
  public void specifiedEventTypesPassedToProcessor() {
    mockConfig();
    String[] projectCreatedOnly = new String[] {"project-created"};
    when(config.getStringList(eq(REMOTE), eq(FOO), eq(EVENT))).thenReturn(projectCreatedOnly);
    when(processor.shouldPost(eq(projectCreatedOnly))).thenReturn(true);

    eventHandler.onEvent(PROJECT_CREATED);
    verify(processor, times(1)).shouldPost(eq(projectCreatedOnly));
  }

  @Test
  public void shouldCallProcessWhenShouldProcessIsTrue() {
    mockConfig();
    when(processor.shouldPost(any(String[].class))).thenReturn(true);

    eventHandler.onEvent(PROJECT_CREATED);
    verify(processor, times(1)).shouldPost(any(String[].class));
    verify(postTask, times(1)).schedule();
  }

  @Test
  public void shouldNotCallProcessWhenWebhookTypeIsMisconfigured() {
    mockConfig();
    when(provider.getFactory(eq(config), eq(FOO))).thenReturn(Optional.empty());

    eventHandler.onEvent(PROJECT_CREATED);
    verifyZeroInteractions(processor);
    verifyZeroInteractions(postTask);
  }

  @Test
  public void shouldNotCallProcessWhenShouldProcessIsFalse() {
    mockConfig();
    when(processor.shouldPost(any(String[].class))).thenReturn(false);

    eventHandler.onEvent(PROJECT_CREATED);
    verify(processor, times(1)).shouldPost(any(String[].class));
    verifyZeroInteractions(postTask);
  }

  @Test
  public void nonProjectEventNotPosted() {
    Event nonProjectEvent = new Event("non-project-event") {};
    eventHandler.onEvent(nonProjectEvent);
    verifyZeroInteractions(processor);
    verifyZeroInteractions(postTask);
  }

  private void mockConfig() {
    when(config.getSubsections(eq(REMOTE))).thenReturn(ImmutableSet.of(FOO));
    when(config.getString(eq(REMOTE), eq(FOO), eq(URL))).thenReturn(FOO_URL);
    when(config.getStringList(eq(REMOTE), eq(FOO), eq(EVENT))).thenReturn(EMPTY_EVENTS_LIST);
  }
}
