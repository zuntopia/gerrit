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

package com.google.gerrit.server.git.validators;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.entities.Account;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.AccountConfig;
import com.google.gerrit.server.account.AccountProperties;
import com.google.gerrit.server.config.AllUsersName;
import com.google.gerrit.server.git.ValidationError;
import com.google.gerrit.server.mail.send.OutgoingEmailValidator;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Validator that is used to ensure that new commits on any ref in {@code refs/users} are conforming
 * to the NoteDb format for accounts. Used when a user pushes to one of the refs in {@code
 * refs/users} manually.
 */
public class AccountValidator {

  private final Provider<IdentifiedUser> self;
  private final AllUsersName allUsersName;
  private final OutgoingEmailValidator emailValidator;

  @Inject
  public AccountValidator(
      Provider<IdentifiedUser> self,
      AllUsersName allUsersName,
      OutgoingEmailValidator emailValidator) {
    this.self = self;
    this.allUsersName = allUsersName;
    this.emailValidator = emailValidator;
  }

  /**
   * Returns a list of validation messages. An empty list means that there were no issues found. If
   * the list is non-empty, the commit will be rejected.
   */
  public List<String> validate(
      Account.Id accountId,
      Repository allUsersRepo,
      RevWalk rw,
      @Nullable ObjectId oldId,
      ObjectId newId)
      throws IOException {
    Optional<Account> oldAccount = Optional.empty();
    if (oldId != null && !ObjectId.zeroId().equals(oldId)) {
      try {
        oldAccount = loadAccount(accountId, allUsersRepo, rw, oldId, null);
      } catch (ConfigInvalidException e) {
        // ignore, maybe the new commit is repairing it now
      }
    }

    ImmutableList.Builder<String> messages = ImmutableList.builder();
    Optional<Account> newAccount;
    try {
      newAccount = loadAccount(accountId, allUsersRepo, rw, newId, messages);
    } catch (ConfigInvalidException e) {
      return ImmutableList.of(
          String.format(
              "commit '%s' has an invalid '%s' file for account '%s': %s",
              newId.name(), AccountProperties.ACCOUNT_CONFIG, accountId.get(), e.getMessage()));
    }

    if (!newAccount.isPresent()) {
      return ImmutableList.of(String.format("account '%s' does not exist", accountId.get()));
    }

    if (!newAccount.get().isActive() && accountId.equals(self.get().getAccountId())) {
      messages.add("cannot deactivate own account");
    }

    String newPreferredEmail = newAccount.get().preferredEmail();
    if (newPreferredEmail != null
        && (!oldAccount.isPresent()
            || !newPreferredEmail.equals(oldAccount.get().preferredEmail()))) {
      if (!emailValidator.isValid(newPreferredEmail)) {
        messages.add(
            String.format(
                "invalid preferred email '%s' for account '%s'",
                newPreferredEmail, accountId.get()));
      }
    }

    return messages.build();
  }

  private Optional<Account> loadAccount(
      Account.Id accountId,
      Repository allUsersRepo,
      RevWalk rw,
      ObjectId commit,
      @Nullable ImmutableList.Builder<String> messages)
      throws IOException, ConfigInvalidException {
    rw.reset();
    AccountConfig accountConfig = new AccountConfig(accountId, allUsersName, allUsersRepo);
    accountConfig.load(allUsersName, rw, commit);
    if (messages != null) {
      messages.addAll(
          accountConfig.getValidationErrors().stream()
              .map(ValidationError::getMessage)
              .collect(toSet()));
    }
    return accountConfig.getLoadedAccount();
  }
}
