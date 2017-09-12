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

import com.google.common.base.MoreObjects;
import com.google.gerrit.server.events.ProjectEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface EventProcessor {
  public class Request {
    public final String body;
    public final Map<String, String> headers;

    public Request(String body) {
      this(body, null);
    }

    public Request(String body, Map<String, String> headers) {
      this.body = body;
      this.headers = Optional.ofNullable(headers).orElse(Collections.emptyMap());
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("headers", headers).add("body", body).toString();
    }
  }

  Optional<Request> process(ProjectEvent event, RemoteConfig remote);
}
