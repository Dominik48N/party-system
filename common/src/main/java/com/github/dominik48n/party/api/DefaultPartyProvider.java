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
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.redis.RedisMessageSub;
import com.github.dominik48n.party.redis.RedisSwitchServerSub;
import com.github.dominik48n.party.user.UserManager;
import com.github.dominik48n.party.util.Constants;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

public class DefaultPartyProvider<TUser> implements PartyProvider {

    private final @NotNull DefaultOnlinePlayersProvider<TUser> onlinePlayerProvider;

    private final @NotNull RedisManager redisManager;
    private final @NotNull MessageConfig messageConfig;

    public DefaultPartyProvider(
            final @NotNull RedisManager redisManager,
            final @NotNull UserManager<TUser> userManager,
            final @NotNull MessageConfig messageConfig
    ) {
        this.onlinePlayerProvider = new DefaultOnlinePlayersProvider<>(redisManager, userManager);
        this.redisManager = redisManager;
        this.messageConfig = messageConfig;

        PartyAPI.set(this);
    }

    @Override
    public @NotNull OnlinePlayerProvider onlinePlayerProvider() {
        return this.onlinePlayerProvider;
    }

    @Override
    public @NotNull Optional<UUID> getPartyFromPlayer(final @NotNull UUID uniqueId) throws JsonProcessingException {
        return this.onlinePlayerProvider.get(uniqueId).flatMap(PartyPlayer::partyId);
    }

    @Override
    public void addPlayerToParty(final @NotNull UUID partyId, final @NotNull UUID player) throws JsonProcessingException {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String partyKey = "party:" + partyId;
            final String partyJson = jedis.get(partyKey);
            if (partyJson != null) {
                final Party party = Document.MAPPER.readValue(partyJson, Party.class);
                party.members().add(player);
                jedis.set(partyKey, Document.MAPPER.writeValueAsString(party));
            }

            this.onlinePlayerProvider.updatePartyId(jedis, player, partyId);
        }
    }

    @Override
    public void removePlayerFromParty(
            final @NotNull UUID partyId,
            final @NotNull UUID player,
            final @NotNull String username
    ) throws JsonProcessingException {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String partyKey = "party:" + partyId;
            final String partyJson = jedis.get(partyKey);
            if (partyJson != null) {
                final Party party = Document.MAPPER.readValue(partyJson, Party.class);
                party.members().remove(player);
                jedis.set(partyKey, Document.MAPPER.writeValueAsString(party));
            }

            this.clearPartyRequest(jedis, username);
            this.onlinePlayerProvider.updatePartyId(jedis, player, null);
        }
    }

    @Override
    public void changePartyLeader(
            final @NotNull UUID partyId,
            final @NotNull UUID oldLeader,
            final @NotNull UUID newLeader,
            final int maxMembers
    ) throws JsonProcessingException {
        Preconditions.checkArgument(
                maxMembers >= 0 && maxMembers <= Constants.MAXIMUM_MEMBER_LIMIT,
                "maxMembers cannot be negative!"
        );
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String json = jedis.get("party:" + partyId);
            if (json == null) return; // Party isn't exist.

            final Party party = Document.MAPPER.readValue(json, Party.class);
            party.members().remove(newLeader);
            party.members().add(oldLeader);
            jedis.set("party:" + partyId, Document.MAPPER.writeValueAsString(new Party(partyId, newLeader, party.members(), maxMembers)));
        }
    }

    @Override
    public @NotNull Optional<Party> getParty(final @NotNull UUID id) throws JsonProcessingException {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String json = jedis.get("party:" + id);
            if (json == null) return Optional.empty();

            final Party party = Document.MAPPER.readValue(json, Party.class);
            return Optional.of(party);
        }
    }

    @Override
    public @NotNull Party createParty(final @NotNull UUID leader, final int maxMembers) throws JsonProcessingException, IllegalArgumentException {
        Preconditions.checkArgument(maxMembers >= 0, "maxMembers cannot be negative!");
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            UUID partyId;
            do {
                partyId = UUID.randomUUID();
            } while (jedis.exists("party:" + partyId));

            final Party party = new Party(partyId, leader, Lists.newArrayList(), maxMembers);
            jedis.set("party:" + partyId, Document.MAPPER.writeValueAsString(party));
            return party;
        }
    }

    @Override
    public void sendMessageToParty(final @NotNull Party party, final @NotNull String messageKey, final @NotNull Object... replacements) {
        this.sendMessageToPlayers(party.allMembers(), messageKey, replacements);
    }

    @Override
    public void sendMessageToMembers(final @NotNull Party party, final @NotNull String messageKey, final @NotNull Object... replacements) {
        this.sendMessageToPlayers(party.members(), messageKey, replacements);
    }

    private void sendMessageToPlayers(final @NotNull List<UUID> players, final @NotNull String messageKey, final @NotNull Object... replacements) {
        final Component component = this.messageConfig.getMessage(messageKey, replacements);
        final String message = MiniMessage.miniMessage().serialize(component);
       players.forEach(uuid -> this.redisManager.publish(
                RedisMessageSub.CHANNEL,
                new Document().append("unique_id", uuid.toString()).append("message", message))
        );
    }

    @Override
    public void connectPartyToServer(final @NotNull Party party, final @NotNull String serverName) {
        final Component component = this.messageConfig.getMessage("party.connect_to_server", serverName);
        final String message = MiniMessage.miniMessage().serialize(component);
        party.allMembers().forEach(uuid -> {
            this.redisManager.publish(RedisSwitchServerSub.CHANNEL, new Document().append("unique_id", uuid.toString()).append("server", serverName));
            this.redisManager.publish(RedisMessageSub.CHANNEL, new Document().append("unique_id", uuid.toString()).append("message", message));
        });
    }

    @Override
    public void deleteParty(final @NotNull UUID id) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.del("party:" + id);
        }
    }

    @Override
    public void removePartyRequest(final @NotNull String source, final @NotNull String target) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.del("request:" + source + ":" + target);
        }
    }

    @Override
    public void createPartyRequest(final @NotNull String source, final @NotNull String target, final int expires) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.setex("request:" + source + ":" + target, expires, "");
        }
    }

    private void clearPartyRequest(final @NotNull Jedis jedis, final @NotNull String source) {
        final Set<String> keys = jedis.keys("request:" + source + ":*");
        if (!keys.isEmpty()) jedis.del(keys.toArray(String[]::new));
    }

    @Override
    public void clearPartyRequest(final @NotNull String source) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            this.clearPartyRequest(jedis, source);
        }
    }

    @Override
    public boolean existsPartyRequest(final @NotNull String source, final @NotNull String target) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            return jedis.exists("request:" + source + ":" + target);
        }
    }
}
