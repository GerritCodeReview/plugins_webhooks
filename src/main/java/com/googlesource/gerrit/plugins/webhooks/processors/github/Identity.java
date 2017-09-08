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

package com.googlesource.gerrit.plugins.webhooks.processors.github;

import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.server.account.AccountByEmailCache;
import com.google.gerrit.server.account.AccountCache;
import com.google.gerrit.server.account.AccountState;
import com.google.gerrit.server.data.AccountAttribute;
import com.google.inject.Inject;
import java.util.Set;
import org.eclipse.jgit.lib.PersonIdent;

public class Identity {
  public static class Builder {
    private final AccountCache byId;
    private final AccountByEmailCache byEmail;
    private String name;
    private String email;
    private String username;

    @Inject
    Builder(AccountCache byId, AccountByEmailCache accounts) {
      this.byId = byId;
      this.byEmail = accounts;
    }

    public Identity build() {
      return new Identity(name, email, username);
    }

    public Builder withAccount(AccountAttribute account) {
      this.name = account.name;
      this.email = account.email;
      this.username = account.username;
      return this;
    }

    public Builder withAccount(PersonIdent authorIdent) {
      this.name = authorIdent.getName();
      this.email = authorIdent.getEmailAddress();
      Set<Account.Id> emailAccounts = byEmail.get(authorIdent.getEmailAddress());
      if (emailAccounts.size() == 1) {
        AccountState state = byId.get(emailAccounts.iterator().next());
        this.username = state.getUserName();
      }
      return this;
    }
  }

  public final String name;
  public final String email;
  public final String username;

  protected Identity(String name, String email, String username) {
    this.name = name;
    this.email = email;
    this.username = username;
  }
}
