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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.jgit.lib.Config;

public class RemoteConfig {
  public interface Factory {
    RemoteConfig create(@Assisted("config") Config config, @Assisted("name") String name);
  }

  public static final String REMOTE = "remote";

  static final String CONNECTION_TIMEOUT = "connectionTimeout";
  static final String SOCKET_TIMEOUT = "socketTimeout";
  static final String MAX_TRIES = "maxTries";
  static final String RETRY_INTERVAL = "retryInterval";
  static final String SSL_VERIFY = "sslVerify";

  private final Configuration global;
  private final Config config;
  private final String url;
  private final String name;

  @Inject
  RemoteConfig(
      Configuration global, @Assisted("config") Config config, @Assisted("name") String name) {
    this.global = global;
    this.config = config;
    this.name = name;
    this.url = config.getString(REMOTE, name, "url");
  }

  public String getUrl() {
    return url;
  }

  public String[] getEvents() {
    return config.getStringList(REMOTE, name, "event");
  }

  public int getConnectionTimeout() {
    return config.getInt(REMOTE, name, CONNECTION_TIMEOUT, global.getConnectionTimeout());
  }

  public int getSocketTimeout() {
    return config.getInt(REMOTE, name, SOCKET_TIMEOUT, global.getSocketTimeout());
  }

  public int getMaxTries() {
    return config.getInt(REMOTE, name, MAX_TRIES, global.getMaxTries());
  }

  public int getRetryInterval() {
    return config.getInt(REMOTE, name, RETRY_INTERVAL, global.getRetryInterval());
  }

  public boolean getSslVerify() {
    return config.getBoolean(REMOTE, name, SSL_VERIFY, global.getSslVerify());
  }

  // methods were added in order to make configuration
  // extensible in EvenptProcessor implementations
  public Configuration getGlobal() {
    return global;
  }

  public Config getEffective() {
    return config;
  }

  public String getName() {
    return name;
  }
}
