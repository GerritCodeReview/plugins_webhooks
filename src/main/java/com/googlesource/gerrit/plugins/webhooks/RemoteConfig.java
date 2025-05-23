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

import static com.googlesource.gerrit.plugins.webhooks.Configuration.UNSET_CONFIG_INT;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Arrays;
import java.util.List;
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
    String[] globalAllowedEvents = global.getAllowedEvents();
    String[] remoteEvents = config.getStringList(REMOTE, name, "event");
    if (globalAllowedEvents.length > 0) {
      if (remoteEvents.length > 0) {
        List<String> globalAllowedEventsList = Arrays.asList(globalAllowedEvents);
        return Arrays.stream(remoteEvents)
            .distinct()
            .filter(globalAllowedEventsList::contains)
            .toArray(String[]::new);
      }
      return globalAllowedEvents;
    }
    return remoteEvents;
  }

  public int getConnectionTimeout() {
    int connectionTimeout =
        config.getInt(REMOTE, name, CONNECTION_TIMEOUT, global.getConnectionTimeout());
    return (global.getMaxAllowedConnectionTimeout() > 0)
        ? Math.min(connectionTimeout, global.getMaxAllowedConnectionTimeout())
        : connectionTimeout;
  }

  public int getSocketTimeout() {
    int socketTimeout = config.getInt(REMOTE, name, SOCKET_TIMEOUT, global.getSocketTimeout());
    return (global.getMaxAllowedSocketTimeout() > 0)
        ? Math.min(socketTimeout, global.getMaxAllowedSocketTimeout())
        : socketTimeout;
  }

  public int getMaxTries() {
    int maxAllowedRetries = global.getMaxAllowedTries();
    int maxTries = config.getInt(REMOTE, name, MAX_TRIES, global.getMaxTries());
    return (maxAllowedRetries != UNSET_CONFIG_INT)
        ? Math.min(maxTries, maxAllowedRetries)
        : maxTries;
  }

  public int getRetryInterval() {
    int retryInterval = config.getInt(REMOTE, name, RETRY_INTERVAL, global.getRetryInterval());
    return (global.getMaxAllowedRetryInterval() > 0)
        ? Math.min(retryInterval, global.getMaxAllowedRetryInterval())
        : retryInterval;
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
