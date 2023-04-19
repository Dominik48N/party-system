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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

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

}
