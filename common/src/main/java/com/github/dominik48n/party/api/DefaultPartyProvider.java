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

import com.github.dominik48n.party.api.player.OnlinePlayerProvider;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.redis.RedisMessageSub;
import com.github.dominik48n.party.redis.RedisSwitchServerSub;
import com.github.dominik48n.party.user.UserManager;
import com.google.common.collect.Lists;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

public class DefaultPartyProvider<TUser> implements PartyProvider {

    private final @NotNull DefaultOnlinePlayersProvider<TUser> onlinePlayerProvider;

    private final @NotNull UserManager<TUser> userManager;
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
        this.userManager = userManager;

        PartyAPI.set(this);
    }

    @Override
    public @NotNull OnlinePlayerProvider onlinePlayerProvider() {
        return this.onlinePlayerProvider;
    }

    @Override
    public @NotNull Optional<UUID> getPartyFromPlayer(final @NotNull UUID uniqueId) {
        return this.onlinePlayerProvider.get(uniqueId).flatMap(PartyPlayer::partyId);
    }

    @Override
    public void addPlayerToParty(final @NotNull UUID partyId, final @NotNull UUID player) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String partyKey = "party:" + partyId;
            final String partyJson = jedis.get(partyKey);
            if (partyJson != null) {
                final Party party = Document.GSON.fromJson(partyJson, Party.class);
                party.members().add(player);
                jedis.set(partyKey, Document.GSON.toJson(party));
            }

            this.onlinePlayerProvider.updatePartyId(jedis, player, partyId);
        }
    }

    @Override
    public void removePlayerFromParty(final @NotNull UUID partyId, final @NotNull UUID player) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String partyKey = "party:" + partyId;
            final String partyJson = jedis.get(partyKey);
            if (partyJson != null) {
                final Party party = Document.GSON.fromJson(partyJson, Party.class);
                party.members().remove(player);
                jedis.set(partyKey, Document.GSON.toJson(party));
            }

            this.onlinePlayerProvider.updatePartyId(jedis, player, null);
        }
    }

    @Override
    public @NotNull Optional<Party> getParty(final @NotNull UUID id) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String json = jedis.get("party:" + id);
            if (json == null) return Optional.empty();

            final Party party = Document.GSON.fromJson(json, Party.class);
            return Optional.of(party);
        }
    }

    @Override
    public @NotNull Party createParty(final @NotNull UUID leader) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            UUID partyId;
            do {
                partyId = UUID.randomUUID();
            } while (jedis.exists("party:" + partyId));

            final Party party = new Party(partyId, leader, Lists.newArrayList());
            jedis.set("party:" + partyId, Document.GSON.toJson(party));
            return party;
        }
    }

    @Override
    public void sendMessageToParty(final @NotNull Party party, final @NotNull String messageKey, final @NotNull Object... replacements) {
        final Component component = this.messageConfig.getMessage(messageKey, replacements);
        final String message = MiniMessage.miniMessage().serialize(component);
        party.getAllMembers().forEach(uuid -> this.redisManager.publish(
                RedisMessageSub.CHANNEL,
                new Document().append("unique_id", uuid.toString()).append("message", message))
        );
    }

    @Override
    public void connectPartyToServer(final @NotNull Party party, final @NotNull String serverName) {
        final Component component = this.messageConfig.getMessage("party.connect_to_server", serverName);
        final String message = MiniMessage.miniMessage().serialize(component);
        party.getAllMembers().forEach(uuid -> {
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

    @Override
    public boolean existsPartyRequest(final @NotNull String source, final @NotNull String target) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            return jedis.exists("request:" + source + ":" + target);
        }
    }
}
