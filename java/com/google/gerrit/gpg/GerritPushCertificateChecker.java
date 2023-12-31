// Copyright (C) 2015 The Android Open Source Project
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

package com.google.gerrit.gpg;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.config.AllUsersName;
import com.google.gerrit.server.git.GitRepositoryManager;
import java.io.IOException;
import org.eclipse.jgit.lib.Repository;

@AutoFactory
public class GerritPushCertificateChecker extends PushCertificateChecker {
  private final GitRepositoryManager repoManager;
  private final AllUsersName allUsers;

  GerritPushCertificateChecker(
      @Provided GerritPublicKeyChecker.Factory keyCheckerFactory,
      @Provided GitRepositoryManager repoManager,
      @Provided AllUsersName allUsers,
      IdentifiedUser expectedUser) {
    super(keyCheckerFactory.create().setExpectedUser(expectedUser));
    this.repoManager = repoManager;
    this.allUsers = allUsers;
  }

  @Override
  protected Repository getRepository() throws IOException {
    return repoManager.openRepository(allUsers);
  }

  @Override
  protected boolean shouldClose(Repository repo) {
    return true;
  }
}
