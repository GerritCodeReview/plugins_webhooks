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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Configuration {
  private static final Logger log = LoggerFactory.getLogger(Configuration.class);

  private static final int DEFAULT_TIMEOUT_MS = 5000;
  private static final int DEFAULT_MAX_TRIES = 5;
  private static final int DEFAULT_RETRY_INTERVAL = 1000;
  private static final int DEFAULT_THREAD_POOL_SIZE = 2;
  private static final boolean DEFAULT_SSL_VERIFY = false;

  private final int connectionTimeout;
  private final int socketTimeout;
  private final int maxTries;
  private final int retryInterval;
  private final int threadPoolSize;
  private final boolean sslVerify;

  @Inject
  protected Configuration(PluginConfigFactory config, @PluginName String pluginName) {
    PluginConfig cfg = config.getFromGerritConfig(pluginName, true);
    connectionTimeout = getInt(cfg, RemoteConfig.CONNECTION_TIMEOUT, DEFAULT_TIMEOUT_MS);
    socketTimeout = getInt(cfg, RemoteConfig.SOCKET_TIMEOUT, DEFAULT_TIMEOUT_MS);
    maxTries = getInt(cfg, RemoteConfig.MAX_TRIES, DEFAULT_MAX_TRIES);
    retryInterval = getInt(cfg, RemoteConfig.RETRY_INTERVAL, DEFAULT_RETRY_INTERVAL);
    threadPoolSize = getInt(cfg, "threadPoolSize", DEFAULT_THREAD_POOL_SIZE);
    sslVerify = getBoolean(cfg, RemoteConfig.SSL_VERIFY, DEFAULT_SSL_VERIFY);
  }

  protected boolean getBoolean(PluginConfig cfg, String name, boolean defaultValue) {
    try {
      return cfg.getBoolean(name, defaultValue);
    } catch (IllegalArgumentException e) {
      logError(name, "boolean", e);
      throw e;
    }
  }

  protected int getInt(PluginConfig cfg, String name, int defaultValue) {
    try {
      return cfg.getInt(name, defaultValue);
    } catch (IllegalArgumentException e) {
      logError(name, "integer", e);
      throw e;
    }
  }

  protected void logError(String name, String type, Exception e) {
    log.error("invalid value for{}", name);
    log.debug("Failed retrieve {} value: {}", type, e.getMessage(), e);
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
