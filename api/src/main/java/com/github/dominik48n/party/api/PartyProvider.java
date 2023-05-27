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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.player.OnlinePlayerProvider;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * The interface for a PartyProvider, which provides methods to manage parties.
 */
public interface PartyProvider {

    /**
     * Returns the provider for retrieving online players.
     *
     * @return the provider for retrieving online players
     */
    @NotNull OnlinePlayerProvider onlinePlayerProvider();

    /**
     * Gets the party ID associated with a player's UUID.
     *
     * @param uniqueId the {@link UUID} of the player
     *
     * @return an optional containing the party ID, or empty if the player is not in a party
     */
    @NotNull Optional<UUID> getPartyFromPlayer(final @NotNull UUID uniqueId) throws JsonProcessingException;

    /**
     * Adds a player to a party.
     *
     * @param partyId the {@link UUID} of the party
     * @param player  the {@link UUID} of the player to add to the party
     */
    void addPlayerToParty(final @NotNull UUID partyId, final @NotNull UUID player) throws JsonProcessingException;

    /**
     * Removes a player from a party.
     *
     * @param partyId  the {@link UUID} of the {@link Party}
     * @param player   the {@link UUID} of the player to remove from the party
     * @param username the username of the player to remove from the party
     */
    void removePlayerFromParty(final @NotNull UUID partyId, final @NotNull UUID player, final @NotNull String username) throws JsonProcessingException;

    /**
     * Changes the leader of a party and set the new max members limit to 5.
     *
     * @param partyId   the {@link UUID} of the party
     * @param oldLeader the {@link UUID} of the current party leader
     * @param newLeader the {@link UUID} of the new party leader
     */
    default void changePartyLeader(
            final @NotNull UUID partyId,
            final @NotNull UUID oldLeader,
            final @NotNull UUID newLeader
    ) throws JsonProcessingException {
        this.changePartyLeader(partyId, oldLeader, newLeader, 5);
    }

    /**
     * Changes the leader of a party.
     *
     * @param partyId    the {@link UUID} of the party
     * @param oldLeader  the {@link UUID} of the current party leader
     * @param newLeader  the {@link UUID} of the new party leader
     * @param maxMembers the new limit of members (can't be negative!)
     *
     * @throws IllegalArgumentException if {@code maxMembers} is negative
     */
    void changePartyLeader(
            final @NotNull UUID partyId,
            final @NotNull UUID oldLeader,
            final @NotNull UUID newLeader,
            final int maxMembers
    ) throws JsonProcessingException, IllegalArgumentException;

    /**
     * Gets the party associated with a given ID.
     *
     * @param id the {@link UUID} of the party
     *
     * @return an optional containing the party, or empty if the party does not exist
     */
    @NotNull Optional<Party> getParty(final @NotNull UUID id) throws JsonProcessingException;

    /**
     * Creates a new party with the given leader with the members limit of 5.
     *
     * @param leader the {@link UUID} of the party leader
     *
     * @return the new {@link Party}
     */
    default @NotNull Party createParty(final @NotNull UUID leader) throws JsonProcessingException {
        return this.createParty(leader, 5);
    }

    /**
     * Creates a new party with the given leader.
     *
     * @param leader     the {@link UUID} of the party leader
     * @param maxMembers limit of party members (can't be negative!)
     *
     * @return the new {@link Party}
     *
     * @throws IllegalArgumentException if {@code maxMembers} is negative
     */
    @NotNull Party createParty(final @NotNull UUID leader, final int maxMembers) throws JsonProcessingException, IllegalArgumentException;

    /**
     * Sends a message to all members and leader of a party.
     *
     * @param party        the {@link Party} to send the message to
     * @param messageKey   the key of the message to send
     * @param replacements the replacements for any placeholders in the message
     */
    void sendMessageToParty(final @NotNull Party party, final @NotNull String messageKey, final @NotNull Object... replacements);

    /**
     * Sends a message to all members of a party.
     *
     * @param party        the {@link Party} to send the message to
     * @param messageKey   the key of the message to send
     * @param replacements the replacements for any placeholders in the message
     */
    void sendMessageToMembers(final @NotNull Party party, final @NotNull String messageKey, final @NotNull Object... replacements);

    /**
     * Connects a party to a server.
     *
     * @param party      the {@link Party} to connect to the server
     * @param serverName the name of the server to connect to
     */
    void connectPartyToServer(final @NotNull Party party, final @NotNull String serverName);

    /**
     * Deletes a party with the given ID.
     *
     * @param id the {@link UUID} of the party to delete
     */
    void deleteParty(final @NotNull UUID id);

    /**
     * Removes a pending party request from the source player to the target player.
     *
     * @param source the username of the player who sent the request
     * @param target the username of the player who received the request
     */
    void removePartyRequest(final @NotNull String source, final @NotNull String target);

    /**
     * Creates a new party request from the source player to the target player.
     *
     * @param source  the username of the player who sent the request
     * @param target  the username of the player who received the request
     * @param expires the number of seconds until the request expires
     */
    void createPartyRequest(final @NotNull String source, final @NotNull String target, final int expires);

    /**
     * Clears all pending party requests sent by the specified player.
     *
     * @param source the username of the player whose requests should be cleared
     */
    void clearPartyRequest(final @NotNull String source);

    /**
     * Checks whether a pending party request exists from the source player to the target player.
     *
     * @param source the username of the player who sent the request
     * @param target the username of the player who received the request
     *
     * @return true if a request exists, false otherwise
     */
    boolean existsPartyRequest(final @NotNull String source, final @NotNull String target);
}
