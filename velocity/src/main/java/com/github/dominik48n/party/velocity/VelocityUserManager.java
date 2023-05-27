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

package com.github.dominik48n.party.velocity;

import com.github.dominik48n.party.util.Constants;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class VelocityUserManager extends UserManager<Player> {

    private final @NotNull ProxyPluginConfig config;
    private final @NotNull ProxyServer server;
    private final @NotNull Logger logger;

    VelocityUserManager(
            final @NotNull RedisManager redisManager,
            final @NotNull ProxyPluginConfig config,
            final @NotNull ProxyServer server,
            final @NotNull Logger logger
    ) {
        super(redisManager);
        this.config = config;
        this.server = server;
        this.logger = logger;
    }

    @Override
    public void sendMessageToLocalUser(final @NotNull UUID uniqueId, final @NotNull Component component) {
        this.server.getPlayer(uniqueId).ifPresent(player -> player.sendMessage(component));
    }

    @Override
    public void connectToServer(final @NotNull UUID uniqueId, final @NotNull String serverName) {
        this.server.getPlayer(uniqueId).ifPresent(player -> {
            this.server.getServer(serverName).ifPresentOrElse(server -> {
                if (player.getCurrentServer().isPresent() && player.getCurrentServer().get().getServer().equals(server)) return;
                player.createConnectionRequest(server).connect();
            }, () -> this.logger.error("Failed to send {} to {}, because the server is unknown on this proxy.", uniqueId, serverName));
        });
    }

    @Override
    protected void sendMessage(final @NotNull Player player, final @NotNull Component component) {
        player.sendMessage(component);
    }

    @Override
    protected int memberLimit(final @NotNull Player player) {
        if (!this.config.partyConfig().useMemberLimit()) return -1;

        int result = -1;
        for (int i = Constants.MAXIMUM_MEMBER_LIMIT; i > 0; i--) {
            if (!player.hasPermission(Constants.MEMBER_LIMIT_PERMISSION_PREFIX + i) || result > i) continue;
            result = i;
        }
        return result == -1 ? this.config.partyConfig().defaultMemberLimit() : result;
    }

    @Override
    protected @NotNull String playerName(final @NotNull Player player) {
        return player.getUsername();
    }

    @Override
    protected @NotNull UUID playerUUID(final @NotNull Player player) {
        return player.getUniqueId();
    }

    @Override
    protected @NotNull MessageConfig messageConfig() {
        return this.config.messageConfig();
    }
}
