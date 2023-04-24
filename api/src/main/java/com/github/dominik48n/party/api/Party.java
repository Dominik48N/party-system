/*
 * Copyright 2023 Dominik48N
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dominik48n.party.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a party, a group of players in a game who play together.
 */
public record Party(@NotNull UUID id, @NotNull UUID leader, @NotNull List<UUID> members) {

    /**
     * Returns a list of all members of this party, including the leader.
     *
     * @return a list of {@link UUID}s representing all members of this party
     */
    @JsonIgnore
    public @NotNull List<UUID> allMembers() {
        final List<UUID> allMembers = new ArrayList<>(this.members);
        allMembers.add(leader);
        return allMembers;
    }

    /**
     * Determines whether the specified {@link UUID} represents the leader of this party.
     *
     * @param uniqueId the unique ID to check
     *
     * @return true if the unique ID represents the leader of this party, false otherwise
     */
    public boolean isLeader(final @NotNull UUID uniqueId) {
        return this.leader.equals(uniqueId);
    }
}
