// Copyright (C) 2016 The Android Open Source Project
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

package com.google.gerrit.server.notedb;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.metrics.Description;
import com.google.gerrit.metrics.Description.Units;
import com.google.gerrit.metrics.Field;
import com.google.gerrit.metrics.MetricMaker;
import com.google.gerrit.metrics.Timer2;
import com.google.gerrit.server.config.AllProjectsName;
import com.google.gerrit.server.config.AllUsersName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.extensions.events.GitReferenceUpdated;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.logging.Metadata;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jgit.lib.Config;

@Singleton
public class Sequences {
  private static final String SECTION_NOTEDB = "noteDb";
  private static final String KEY_SEQUENCE_BATCH_SIZE = "sequenceBatchSize";
  private static final int DEFAULT_ACCOUNTS_SEQUENCE_BATCH_SIZE = 1;
  private static final int DEFAULT_CHANGES_SEQUENCE_BATCH_SIZE = 20;

  public static final String NAME_ACCOUNTS = "accounts";
  public static final String NAME_GROUPS = "groups";
  public static final String NAME_CHANGES = "changes";

  public static final int FIRST_ACCOUNT_ID = 1000000;
  public static final int FIRST_GROUP_ID = 1;
  public static final int FIRST_CHANGE_ID = 1;

  private enum SequenceType {
    ACCOUNTS,
    CHANGES,
    GROUPS;
  }

  private final RepoSequence accountSeq;
  private final RepoSequence changeSeq;
  private final RepoSequence groupSeq;
  private final Timer2<SequenceType, Boolean> nextIdLatency;
  private final int accountBatchSize;
  private final int changeBatchSize;
  private final int groupBatchSize = 1;

  @Inject
  public Sequences(
      @GerritServerConfig Config cfg,
      GitRepositoryManager repoManager,
      GitReferenceUpdated gitRefUpdated,
      AllProjectsName allProjects,
      AllUsersName allUsers,
      MetricMaker metrics) {

    accountBatchSize =
        cfg.getInt(
            SECTION_NOTEDB,
            NAME_ACCOUNTS,
            KEY_SEQUENCE_BATCH_SIZE,
            DEFAULT_ACCOUNTS_SEQUENCE_BATCH_SIZE);
    accountSeq =
        new RepoSequence(
            repoManager,
            gitRefUpdated,
            allUsers,
            NAME_ACCOUNTS,
            () -> FIRST_ACCOUNT_ID,
            accountBatchSize);

    changeBatchSize =
        cfg.getInt(
            SECTION_NOTEDB,
            NAME_CHANGES,
            KEY_SEQUENCE_BATCH_SIZE,
            DEFAULT_CHANGES_SEQUENCE_BATCH_SIZE);
    changeSeq =
        new RepoSequence(
            repoManager,
            gitRefUpdated,
            allProjects,
            NAME_CHANGES,
            () -> FIRST_CHANGE_ID,
            changeBatchSize);

    groupSeq =
        new RepoSequence(
            repoManager,
            gitRefUpdated,
            allUsers,
            NAME_GROUPS,
            () -> FIRST_GROUP_ID,
            groupBatchSize);

    nextIdLatency =
        metrics.newTimer(
            "sequence/next_id_latency",
            new Description("Latency of requesting IDs from repo sequences")
                .setCumulative()
                .setUnit(Units.MILLISECONDS),
            Field.ofEnum(SequenceType.class, "sequence", Metadata.Builder::noteDbSequenceType)
                .description("The sequence from which IDs were retrieved.")
                .build(),
            Field.ofBoolean("multiple", Metadata.Builder::multiple)
                .description("Whether more than one ID was retrieved.")
                .build());
  }

  public int nextAccountId() {
    try (Timer2.Context<SequenceType, Boolean> timer =
        nextIdLatency.start(SequenceType.ACCOUNTS, false)) {
      return accountSeq.next();
    }
  }

  public int nextChangeId() {
    try (Timer2.Context<SequenceType, Boolean> timer =
        nextIdLatency.start(SequenceType.CHANGES, false)) {
      return changeSeq.next();
    }
  }

  public ImmutableList<Integer> nextChangeIds(int count) {
    try (Timer2.Context<SequenceType, Boolean> timer =
        nextIdLatency.start(SequenceType.CHANGES, count > 1)) {
      return changeSeq.next(count);
    }
  }

  public int nextGroupId() {
    try (Timer2.Context<SequenceType, Boolean> timer =
        nextIdLatency.start(SequenceType.GROUPS, false)) {
      return groupSeq.next();
    }
  }

  public int changeBatchSize() {
    return changeBatchSize;
  }

  public int groupBatchSize() {
    return groupBatchSize;
  }

  public int accountBatchSize() {
    return accountBatchSize;
  }

  public int currentChangeId() {
    return changeSeq.current();
  }

  public int currentAccountId() {
    return accountSeq.current();
  }

  public int currentGroupId() {
    return groupSeq.current();
  }

  public int lastChangeId() {
    return changeSeq.last();
  }

  public int lastGroupId() {
    return groupSeq.last();
  }

  public int lastAccountId() {
    return accountSeq.last();
  }

  public void setChangeIdValue(int value) {
    changeSeq.storeNew(value);
  }

  public void setAccountIdValue(int value) {
    accountSeq.storeNew(value);
  }

  public void setGroupIdValue(int value) {
    groupSeq.storeNew(value);
  }
}
