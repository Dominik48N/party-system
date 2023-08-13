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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class UserManager<TUser> {

   private final @NotNull Map<TUser, PartyPlayer> cachedPlayers = Maps.newConcurrentMap();
   private final @NotNull RedisManager redisManager;

   protected UserManager(final @NotNull RedisManager redisManager) {
      this.redisManager = redisManager;
   }

   void cachePlayer(final @NotNull TUser user, final @NotNull PartyPlayer player) {
      this.cachedPlayers.put(user, player);
   }

   public @NotNull Optional<PartyPlayer> getPlayer(final @NotNull TUser user) {
      return Optional.ofNullable(this.cachedPlayers.get(user));
   }

   public @NotNull Optional<PartyPlayer> userFromCache(final @NotNull UUID playerId) {
      return this.cachedPlayers.values().stream().filter(player -> player.uniqueId().equals(playerId)).findAny();
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

   protected abstract int memberLimit(final @NotNull TUser user);

   protected abstract @NotNull String playerName(final @NotNull TUser user);

   protected abstract @NotNull UUID playerUUID(final @NotNull TUser user);

   protected abstract @NotNull MessageConfig messageConfig();
}
