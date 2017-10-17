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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.inject.util.Providers;
import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class PostTaskTest {

  private static final String WEBHOOK_URL = "webhook-url";
  private static final Map<String, String> HEADERS = Collections.emptyMap();
  private static final String BODY = "body";
  private static final Optional<EventProcessor.Request> CONTENT =
      Optional.of(new EventProcessor.Request(BODY, HEADERS));
  private static final HttpResult OK_RESULT = new HttpResult(true, "");
  private static final HttpResult ERR_RESULT = new HttpResult(false, "");
  private static final int RETRY_INTERVAL = 100;
  private static final int MAX_TRIES = 3;

  @Mock private ProjectCreatedEvent projectCreated;

  @Mock private RemoteConfig remote;

  @Mock private Configuration cfg;

  @Mock private HttpSession session;

  @Mock private ScheduledThreadPoolExecutor executor;

  @Mock private EventProcessor processor;

  private PostTask task;

  @Before
  public void setup() {
    when(remote.getRetryInterval()).thenReturn(RETRY_INTERVAL);
    when(remote.getMaxTries()).thenReturn(MAX_TRIES);
    when(remote.getUrl()).thenReturn(WEBHOOK_URL);
    when(processor.process(eq(projectCreated), eq(remote))).thenReturn(CONTENT);
    task = new PostTask(executor, Providers.of(session), processor, projectCreated, remote);
  }

  @Test
  public void noScheduleOnEmptyBody() throws Exception {
    when(processor.process(eq(projectCreated), eq(remote))).thenReturn(Optional.empty());
    task.run();
    verifyZeroInteractions(session);
    verifyZeroInteractions(executor);
  }

  @Test
  public void noRescheduleOnSuccess() throws IOException {
    when(session.post(WEBHOOK_URL, HEADERS, remote, BODY)).thenReturn(OK_RESULT);
    task.run();
    verifyZeroInteractions(executor);
  }

  @Test
  public void noRescheduleOnNonRecoverableException() throws IOException {
    when(session.post(WEBHOOK_URL, HEADERS, remote, BODY)).thenThrow(SSLException.class);
    task.run();
    verifyZeroInteractions(executor);
  }

  @Test
  public void rescheduleOnError() throws IOException {
    when(session.post(WEBHOOK_URL, HEADERS, remote, BODY)).thenReturn(ERR_RESULT);
    task.run();
    verify(executor, times(1)).schedule(task, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
  }

  @Test
  public void rescheduleOnRecoverableException() throws IOException {
    when(session.post(WEBHOOK_URL, HEADERS, remote, BODY)).thenThrow(IOException.class);
    task.run();
    verify(executor, times(1)).schedule(task, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
  }

  @Test
  public void keepReschedulingMaxTriesTimes() throws IOException {
    when(session.post(WEBHOOK_URL, HEADERS, remote, BODY)).thenThrow(IOException.class);
    when(executor.schedule(task, RETRY_INTERVAL, TimeUnit.MILLISECONDS))
        .then(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                task.run();
                return null;
              }
            });
    task.run();
    verify(executor, times(MAX_TRIES - 1)).schedule(task, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
  }
}
