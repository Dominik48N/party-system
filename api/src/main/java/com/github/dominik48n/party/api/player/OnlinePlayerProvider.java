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

package com.github.dominik48n.party.api.player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides methods for interacting with online players in Redis.
 */
public interface OnlinePlayerProvider {

    /**
     * Retrieves the {@link PartyPlayer} object with the specified name from Redis.
     *
     * @param username the name of the {@link PartyPlayer} to retrieve
     *
     * @return the retrieved {@link PartyPlayer} object, or <i>null</i> if not found
     */
    @NotNull Optional<PartyPlayer> get(final @NotNull String username);

    /**
     * Retrieves a {@link PartyPlayer} from Redis using the specified {@link UUID}.
     *
     * @param uniqueId The {@link UUID} of the {@link PartyPlayer} to retrieve.
     *
     * @return The retrieved {@link PartyPlayer}, or null if the {@link PartyPlayer} does not exist in Redis.
     */
    @NotNull Optional<PartyPlayer> get(final @NotNull UUID uniqueId);

    /**
     * Retrieves multiple {@link PartyPlayer} objects from Redis using their {@link UUID}s.
     *
     * @param uniqueIds A collection of {@link UUID}s of the {@link PartyPlayer}s to retrieve.
     *
     * @return A map of the retrieved {@link PartyPlayer} objects, keyed by their {@link UUID}s.
     */
    @NotNull Map<UUID, PartyPlayer> get(final @NotNull Collection<UUID> uniqueIds);

    /**
     * Retrieves all {@link PartyPlayer} objects from Redis.
     *
     * @return a list of all {@link PartyPlayer} objects in Redis
     */
    @NotNull List<PartyPlayer> all();

    /**
     * Creates a new {@link PartyPlayer} in Redis using the specified {@link PartyPlayer}.
     *
     * @param player The {@link PartyPlayer} to create.
     */
    void login(final @NotNull PartyPlayer player);

    /**
     * Removes a {@link PartyPlayer} from Redis using the specified {@link UUID}.
     *
     * @param uniqueId The {@link UUID} of the {@link PartyPlayer} to remove.
     */
    void logout(final @NotNull UUID uniqueId);

    /**
     * Updates the party ID of the {@link PartyPlayer} with the given {@link UUID} to the specified party ID.
     * If the player's current party ID already matches the specified party ID, no update is performed.
     * If no player with the given unique ID exists, returns {@code false}.
     *
     * @param uniqueId The unique ID of the {@link PartyPlayer} to update.
     * @param partyId  The new party ID to set for the player.
     *
     * @return {@code true} if the player's party ID was updated, {@code false} otherwise.
     */
    boolean updatePartyId(final @NotNull UUID uniqueId, final @Nullable UUID partyId);

}
