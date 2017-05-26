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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;

@RunWith(MockitoJUnitRunner.class)
public class PostTaskTest {

  private static final String WEBHOOK_URL = "webhook-url";
  private static final String BODY = "body";
  private static final HttpResult OK_RESULT = new HttpResult(true, "");
  private static final HttpResult ERR_RESULT = new HttpResult(false, "");
  private static final int RETRY_INTERVAL = 100;
  private static final int MAX_TRIES = 100;

  @Test
  public void noRescheduleOnSuccess() throws IOException {
    HttpSession session = mock(HttpSession.class);
    when(session.post(WEBHOOK_URL, BODY)).thenReturn(OK_RESULT);

    ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);

    PostTask task = new PostTask(
        executor, session, mock(Configuration.class), WEBHOOK_URL, BODY);
    task.run();

    verifyZeroInteractions(executor);
  }

  @Test
  public void noRescheduleOnNonRecoverableException() throws IOException {
    HttpSession session = mock(HttpSession.class);
    when(session.post(WEBHOOK_URL, BODY)).thenThrow(SSLException.class);

    ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);

    PostTask task = new PostTask(
        executor, session, mock(Configuration.class), WEBHOOK_URL, BODY);
    task.run();

    verifyZeroInteractions(executor);
  }

  @Test
  public void rescheduleOnError() throws IOException {
    Configuration cfg = mock(Configuration.class);
    when(cfg.getRetryInterval()).thenReturn(RETRY_INTERVAL);
    when(cfg.getMaxTries()).thenReturn(3);

    HttpSession session = mock(HttpSession.class);
    when(session.post(WEBHOOK_URL, BODY)).thenReturn(ERR_RESULT);

    ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);

    PostTask task = new PostTask(
        executor, session, cfg, WEBHOOK_URL, BODY);
    task.run();

    verify(executor, times(1)).schedule(task, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
  }

  @Test
  public void rescheduleOnRecoverableException() throws IOException {
    Configuration cfg = mock(Configuration.class);
    when(cfg.getRetryInterval()).thenReturn(RETRY_INTERVAL);
    when(cfg.getMaxTries()).thenReturn(3);

    HttpSession session = mock(HttpSession.class);
    when(session.post(WEBHOOK_URL, BODY)).thenThrow(IOException.class);

    ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);

    PostTask task = new PostTask(
        executor, session, cfg, WEBHOOK_URL, BODY);
    task.run();

    verify(executor, times(1)).schedule(task, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
  }

  @Test
  public void keepReschedulingMaxTriesTimes() throws IOException {
    Configuration cfg = mock(Configuration.class);
    when(cfg.getRetryInterval()).thenReturn(RETRY_INTERVAL);
    when(cfg.getMaxTries()).thenReturn(MAX_TRIES);

    HttpSession session = mock(HttpSession.class);
    when(session.post(WEBHOOK_URL, BODY)).thenThrow(IOException.class);

    ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);

    PostTask task = spy(
        new PostTask(executor, session, cfg, WEBHOOK_URL, BODY));

    when(executor.schedule(task, RETRY_INTERVAL, TimeUnit.MILLISECONDS))
        .then(new Answer<Void>() {
          @Override
          public Void answer(InvocationOnMock invocation) throws Throwable {
            task.run();
            return null;
          }
        });

    task.run();

    verify(task, times(MAX_TRIES)).run();
  }
}
