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

import java.util.List;
import java.util.Objects;

public class RemoteInfo {
  String url;
  List<String> events;
  int connectionTimeout;
  int socketTimeout;
  int maxTries;
  int retryInterval;
  boolean sslVerify;

  @Override
  public boolean equals(Object o) {
    if (o instanceof RemoteInfo) {
      RemoteInfo i = (RemoteInfo) o;
      return Objects.equals(url, i.url)
          && Objects.equals(events, i.events)
          && connectionTimeout == i.connectionTimeout
          && socketTimeout == i.socketTimeout
          && maxTries == i.maxTries
          && retryInterval == i.retryInterval
          && sslVerify == i.sslVerify;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        url, events, connectionTimeout, socketTimeout, maxTries, retryInterval, sslVerify);
  }
}
