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

package com.github.dominik48n.party.user;

import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.redis.RedisMessageSub;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public abstract class UserManager<TUser> {

    private final @NotNull Map<TUser, PartyPlayer> cachedPlayers = Maps.newConcurrentMap();
    private final @NotNull RedisManager redisManager;

    protected UserManager(final @NotNull RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    public @NotNull PartyPlayer createPlayer(final @NotNull TUser user) {
        return new User<>(user, this);
    }

    public @NotNull PartyPlayer createOrGetPlayer(final @NotNull TUser user) {
        PartyPlayer player = this.cachedPlayers.get(user);
        if (player == null) {
            player = this.createPlayer(user);
            this.cachedPlayers.put(user, player);
        }
        return player;
    }

    public void removePlayerFromCache(final @NotNull TUser user) {
        this.cachedPlayers.remove(user);
    }

    void sendMessage(final @NotNull UUID uniqueId, final @NotNull Component component) {
        this.redisManager.publish(
                RedisMessageSub.CHANNEL,
                new Document().append("unique_id", uniqueId.toString()).append("message", MiniMessage.miniMessage().serialize(component))
        );
    }

    public abstract void sendMessageToLocalUser(final @NotNull UUID uniqueId, final @NotNull Component component);

    public abstract void connectToServer(final @NotNull UUID uniqueId, final @NotNull String serverName);

    protected abstract void sendMessage(final @NotNull TUser user, final @NotNull Component component);

    protected abstract @NotNull String playerName(final @NotNull TUser user);

    protected abstract @NotNull UUID playerUUID(final @NotNull TUser user);

    protected abstract @NotNull MessageConfig messageConfig();
}
