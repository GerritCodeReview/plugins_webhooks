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
import com.google.inject.Provider;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* HTTP client that doesn't verify remote's SSL certificate */
class DefaultHttpClientProvider extends HttpClientProvider {
  private static final Logger log = LoggerFactory.getLogger(DefaultHttpClientProvider.class);

  static final String DEFAULT = "default";

  @Inject
  protected DefaultHttpClientProvider(Configuration cfg) {
    super(cfg, socketFactoryProvider());
  }

  private static Provider<Registry<ConnectionSocketFactory>> socketFactoryProvider() {
    return () ->
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register(
                "https",
                new SSLConnectionSocketFactory(buildSslContext(), NoopHostnameVerifier.INSTANCE))
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .build();
  }

  private static SSLContext buildSslContext() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[] {new DummyX509TrustManager()};
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, trustAllCerts, null);
      return context;
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      log.warn("Error building SSLContext object", e);
      return null;
    }
  }

  private static class DummyX509TrustManager implements X509TrustManager {
    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
      // no check
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
      // no check
    }
  }
}
