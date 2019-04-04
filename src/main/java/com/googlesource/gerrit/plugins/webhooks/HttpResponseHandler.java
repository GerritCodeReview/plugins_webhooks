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

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import com.google.common.flogger.FluentLogger;
import com.googlesource.gerrit.plugins.webhooks.HttpResponseHandler.HttpResult;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

class HttpResponseHandler implements ResponseHandler<HttpResult> {

  static class HttpResult {
    final boolean successful;
    final String message;

    HttpResult(boolean successful, String message) {
      this.successful = successful;
      this.message = message;
    }
  }

  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  @Override
  public HttpResult handleResponse(HttpResponse response) {
    return new HttpResult(isSuccessful(response), parseResponse(response));
  }

  private boolean isSuccessful(HttpResponse response) {
    int sc = response.getStatusLine().getStatusCode();
    return sc == SC_CREATED || sc == SC_NO_CONTENT || sc == SC_OK;
  }

  private String parseResponse(HttpResponse response) {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      try {
        return EntityUtils.toString(entity);
      } catch (IOException e) {
        log.atSevere().withCause(e).log("Error parsing entity");
      }
    }
    return "";
  }
}
