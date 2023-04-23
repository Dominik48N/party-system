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
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.NetworkUser;
import com.github.dominik48n.party.user.UserManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;

public class DefaultOnlinePlayersProvider<TUser> implements OnlinePlayerProvider {

    private final @NotNull RedisManager redisManager;
    private final @NotNull UserManager<TUser> userManager;

    public DefaultOnlinePlayersProvider(final @NotNull RedisManager redisManager, final @NotNull UserManager<TUser> userManager) {
        this.redisManager = redisManager;
        this.userManager = userManager;
    }

    @Override
    public @NotNull Optional<PartyPlayer> get(final @NotNull String username) throws JsonProcessingException {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final Set<String> keys = jedis.keys("party_player:*");
            for (final String key : keys) {
                final String json = jedis.get(key);
                if (json == null) continue;

                final PartyPlayer player = Document.MAPPER.readValue(json, PartyPlayer.class);
                if (player.name().equalsIgnoreCase(username)) return Optional.of(new NetworkUser<>(player, this.userManager));
            }
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<PartyPlayer> get(final @NotNull UUID uniqueId) throws JsonProcessingException {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String json = jedis.get("party_player:" + uniqueId);
            if (json == null) return Optional.empty();

            final PartyPlayer player = Document.MAPPER.readValue(json, PartyPlayer.class);
            return Optional.of(new NetworkUser<>(player, this.userManager));
        }
    }

    @Override
    public @NotNull Map<UUID, PartyPlayer> get(final @NotNull Collection<UUID> uniqueIds) throws JsonProcessingException {
        final Map<UUID, PartyPlayer> players = Maps.newHashMap();
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String[] keys = uniqueIds.stream()
                    .map(uuid -> "party_player:" + uuid.toString())
                    .toArray(String[]::new);

            for (final String json : jedis.mget(keys)) {
                if (json == null) continue;

                final PartyPlayer player = Document.MAPPER.readValue(json, PartyPlayer.class);
                players.put(player.uniqueId(), player);
            }
        }
        return players;
    }

    @Override
    public @NotNull List<PartyPlayer> all() throws JsonProcessingException {
        final List<PartyPlayer> partyPlayers = Lists.newArrayList();
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final Set<String> keys = jedis.keys("party_player:*");
            for (final String key : keys) {
                final String json = jedis.get(key);
                if (json == null) continue;

                final PartyPlayer player = Document.MAPPER.readValue(json, PartyPlayer.class);
                partyPlayers.add(new NetworkUser<>(player, this.userManager));
            }
        }
        return partyPlayers;
    }

    @Override
    public void login(final @NotNull PartyPlayer player) throws JsonProcessingException {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.set("party_player:" + player.uniqueId(), Document.MAPPER.writeValueAsString(player));
        }
    }

    @Override
    public void logout(final @NotNull UUID uniqueId) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.del("party_player:" + uniqueId);
        }
    }

    boolean updatePartyId(final @NotNull Jedis jedis, final @NotNull UUID uniqueId, final @Nullable UUID partyId) throws JsonProcessingException {
        final String key = "party_player:" + uniqueId;
        final String json = jedis.get(key);
        if (json == null) return false;

        final PartyPlayer existingPlayer = Document.MAPPER.readValue(json, PartyPlayer.class);
        if (existingPlayer.partyId().isPresent() && existingPlayer.partyId().get().equals(partyId)) return false;

        existingPlayer.partyId(partyId);
        jedis.set(key, Document.MAPPER.writeValueAsString(existingPlayer));
        return true;
    }

    @Override
    public boolean updatePartyId(final @NotNull UUID uniqueId, final @Nullable UUID partyId) throws JsonProcessingException {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            return this.updatePartyId(jedis, uniqueId, partyId);
        }
    }
}
