// Copyright (C) 2022 The Android Open Source Project
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

package com.google.gerrit.extensions.events;

import com.google.gerrit.common.Nullable;
import com.google.gerrit.extensions.annotations.ExtensionPoint;
import com.google.gerrit.extensions.common.AccountInfo;
import java.util.Set;

/** Notified when one or more references are modified. */
@ExtensionPoint
public interface GitBatchRefUpdateListener {
  interface Event extends ProjectEvent {
    Set<UpdatedRef> getUpdatedRefs();

    Set<String> getRefNames();

    /** The updater, could be null if it's the server. */
    @Nullable
    AccountInfo getUpdater();
  }

  interface UpdatedRef {
    public String getRefName();

    public String getOldObjectId();

    public String getNewObjectId();

    public boolean isCreate();

    public boolean isDelete();

    public boolean isNonFastForward();
  }

  void onGitBatchRefUpdate(Event event);
}
