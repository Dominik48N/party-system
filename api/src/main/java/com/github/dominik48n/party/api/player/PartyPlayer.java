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

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a player who is connected to the network.
 */
public interface PartyPlayer {

    /**
     * Gets the unique ID of the player.
     *
     * @return The player's unique ID.
     */
    @NotNull UUID uniqueId();

    /**
     * Gets the name of the player.
     *
     * @return The player's name.
     */
    @NotNull String name();

    /**
     * Sends a message to the player using a message key and replacements.
     * The message is retrieved from the party system's configuration using the message key.
     * The replacements are used to replace placeholders in the message, such as {0}, {1}, etc.
     *
     * @param messageKey The key of the message to send.
     * @param replacements The objects to use as replacements in the message.
     */
    void sendMessage(final @NotNull String messageKey, final @NotNull Object... replacements);
}

