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

/* HTTP client that verifies remote's SSL certificate */
class SslVerifyingHttpClientProvider extends HttpClientProvider {
  static final String SSL_VERIFY = "ssl_verify";

  @Inject
  protected SslVerifyingHttpClientProvider(Configuration cfg,
      HttpClientConnectionManagerProvider.Factory mgrFactory,
      SslVerifyingConnectionSocketFactoryProvider socket) {
    super(cfg, mgrFactory.create(socket.get()));
  }
}
