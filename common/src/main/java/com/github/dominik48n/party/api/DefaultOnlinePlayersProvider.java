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
import com.github.dominik48n.party.redis.RedisManager;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

public class DefaultOnlinePlayersProvider implements OnlinePlayerProvider {

    private final @NotNull RedisManager redisManager;

    public DefaultOnlinePlayersProvider(final @NotNull RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    @Override
    public @NotNull Optional<PartyPlayer> get(final @NotNull String username) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final Set<String> keys = jedis.keys("party_player:*");
            for (final String key : keys) {
                final String json = jedis.get(key);
                if (json == null) continue;

                final PartyPlayer player = Document.GSON.fromJson(json, PartyPlayer.class);
                if (player.name().equalsIgnoreCase(username)) return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<PartyPlayer> get(final @NotNull UUID uniqueId) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final String json = jedis.get("party_player:" + uniqueId);
            if (json == null) return Optional.empty();

            final PartyPlayer player = Document.GSON.fromJson(json, PartyPlayer.class);
            return Optional.of(player);
        }
    }

    @Override
    public @NotNull List<PartyPlayer> all() {
        final List<PartyPlayer> partyPlayers = Lists.newArrayList();
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            final Set<String> keys = jedis.keys("party_player:*");
            for (final String key : keys) {
                final String json = jedis.get(key);
                if (json != null) partyPlayers.add(Document.GSON.fromJson(json, PartyPlayer.class));
            }
        }
        return partyPlayers;
    }

    @Override
    public void login(final @NotNull PartyPlayer player) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.set("party_player:" + player.uniqueId(), Document.GSON.toJson(player));
        }
    }

    @Override
    public void logout(final @NotNull UUID uniqueId) {
        try (final Jedis jedis = this.redisManager.jedisPool().getResource()) {
            jedis.del("party_player:" + uniqueId);
        }
    }
}
