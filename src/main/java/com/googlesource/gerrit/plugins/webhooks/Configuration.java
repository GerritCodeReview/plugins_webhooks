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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Configuration {
  public static final int DEFAULT_TIMEOUT_MS = 5000;
  public static final int DEFAULT_MAX_TRIES = 5;
  public static final int DEFAULT_RETRY_INTERVAL = 1000;
  private static final int DEFAULT_THREAD_POOL_SIZE = 2;
  public static final boolean DEFAULT_SSL_VERIFY = false;

  private final int connectionTimeout;
  private final int socketTimeout;
  private final int maxTries;
  private final int retryInterval;
  private final int threadPoolSize;
  private final boolean sslVerify;

  @Inject
  protected Configuration(PluginConfigFactory config, @PluginName String pluginName) {
    PluginConfig cfg = config.getFromGerritConfig(pluginName, true);
    connectionTimeout = cfg.getInt(RemoteConfig.CONNECTION_TIMEOUT, DEFAULT_TIMEOUT_MS);
    socketTimeout = cfg.getInt(RemoteConfig.SOCKET_TIMEOUT, DEFAULT_TIMEOUT_MS);
    maxTries = cfg.getInt(RemoteConfig.MAX_TRIES, DEFAULT_MAX_TRIES);
    retryInterval = cfg.getInt(RemoteConfig.RETRY_INTERVAL, DEFAULT_RETRY_INTERVAL);
    threadPoolSize = cfg.getInt("threadPoolSize", DEFAULT_THREAD_POOL_SIZE);
    sslVerify = cfg.getBoolean(RemoteConfig.SSL_VERIFY, DEFAULT_SSL_VERIFY);
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public int getMaxTries() {
    return maxTries;
  }

  public int getRetryInterval() {
    return retryInterval;
  }

  public int getSocketTimeout() {
    return socketTimeout;
  }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public boolean getSslVerify() {
    return sslVerify;
  }
}
