// Copyright (C) 2012 The Android Open Source Project
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

package com.google.gerrit.server.account;

import com.google.gerrit.common.Nullable;
import com.google.gerrit.entities.AccountGroup;
import com.google.gerrit.entities.GroupDescription;
import com.google.gerrit.entities.GroupReference;
import com.google.gerrit.extensions.annotations.ExtensionPoint;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.project.ProjectState;
import java.util.Collection;

/** Implementations of GroupBackend provide lookup and membership accessors to a group system. */
@ExtensionPoint
public interface GroupBackend {
  /** Returns {@code true} if the backend can operate on the UUID. */
  boolean handles(AccountGroup.UUID uuid);

  /**
   * Looks up a group in the backend. If the group does not exist, null is returned.
   *
   * @param uuid the group identifier
   * @return the group
   */
  @Nullable
  GroupDescription.Basic get(AccountGroup.UUID uuid);

  /** Returns suggestions for the group name sorted by name. */
  Collection<GroupReference> suggest(String name, @Nullable ProjectState project);

  /** Returns the group membership checker for the backend. */
  GroupMembership membershipsOf(CurrentUser user);

  /** Returns {@code true} if the group with the given UUID is visible to all registered users. */
  boolean isVisibleToAll(AccountGroup.UUID uuid);

  default boolean isOrContainsExternalGroup(AccountGroup.UUID groupId) {
    if (groupId != null) {
      GroupDescription.Basic groupDescription = get(groupId);
      if (!(groupDescription instanceof GroupDescription.Internal)
          || containsExternalSubGroups((GroupDescription.Internal) groupDescription)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsExternalSubGroups(GroupDescription.Internal internalGroup) {
    for (AccountGroup.UUID subGroupUuid : internalGroup.getSubgroups()) {
      GroupDescription.Basic subGroupDescription = get(subGroupUuid);
      if (!(subGroupDescription instanceof GroupDescription.Internal)) {
        return true;
      }
      if (containsExternalSubGroups((GroupDescription.Internal) subGroupDescription)) {
        return true;
      }
    }
    return false;
  }
}
