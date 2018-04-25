// Copyright (C) 2018 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.webhooks.rest;

import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.webhooks.RemoteConfig;
import java.util.Arrays;

@Singleton
public class GetRemote implements RestReadView<RemoteResource> {

  private final Permissions permissions;

  @Inject
  GetRemote(Permissions permissions) {
    this.permissions = permissions;
  }

  @Override
  public RemoteInfo apply(RemoteResource rsrc)
      throws NoSuchProjectException, ResourceNotFoundException, AuthException {
    if (!permissions.canRead(rsrc.getProject())) {
      throw new AuthException("not allowed to read webhooks");
    }
    return fromRemoteConfig(rsrc.getRemoteConfig());
  }

  static RemoteInfo fromRemoteConfig(RemoteConfig rc) {
    RemoteInfo info = new RemoteInfo();
    info.url = rc.getUrl();
    info.events = Arrays.asList(rc.getEvents());
    info.connectionTimeout = rc.getConnectionTimeout();
    info.socketTimeout = rc.getSocketTimeout();
    info.maxTries = rc.getMaxTries();
    info.retryInterval = rc.getRetryInterval();
    info.sslVerify = rc.getSslVerify();
    return info;
  }
}
