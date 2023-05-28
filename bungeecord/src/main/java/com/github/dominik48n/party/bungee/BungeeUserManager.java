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

package com.github.dominik48n.party.bungee;

import com.github.dominik48n.party.util.Constants;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.user.UserManager;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

public class BungeeUserManager extends UserManager<ProxiedPlayer> {

    private final @NotNull ProxyPluginConfig config;
    private final @NotNull PartyBungeePlugin plugin;

    BungeeUserManager(final @NotNull PartyBungeePlugin plugin) {
        super(plugin.redisManager());
        this.config = plugin.config();
        this.plugin = plugin;
    }

    @Override
    public void sendMessageToLocalUser(final @NotNull UUID uniqueId, final @NotNull Component component) {
        Optional.ofNullable(this.plugin.getProxy().getPlayer(uniqueId)).ifPresent(player -> this.sendMessage(player, component));
    }

    @Override
    public void connectToServer(final @NotNull UUID uniqueId, final @NotNull String serverName) {
        Optional.ofNullable(this.plugin.getProxy().getPlayer(uniqueId)).ifPresent(player -> {
            final ServerInfo serverInfo = this.plugin.getProxy().getServerInfo(serverName);
            if (serverInfo == null) {
                this.plugin.getLogger().log(
                        Level.SEVERE,
                        "Failed to send {0} to {1}, because the server is unknown on this proxy.",
                        new Object[] {uniqueId, serverName}
                );
                return;
            }

            if (!player.getServer().getInfo().getName().equals(serverInfo.getName())) player.connect(serverInfo);
        });
    }

    @Override
    protected void sendMessage(final @NotNull ProxiedPlayer player, final @NotNull Component component) {
        this.plugin.audiences().player(player).sendMessage(component);
    }

    @Override
    protected int memberLimit(final @NotNull ProxiedPlayer player) {
        if (!this.config.partyConfig().useMemberLimit()) return -1;

        int result = -1;
        for (int i = Constants.MAXIMUM_MEMBER_LIMIT; i > 0; i--) {
            if (!player.hasPermission(Constants.MEMBER_LIMIT_PERMISSION_PREFIX + i) || result > i) continue;
            result = i;
        }
        return result == -1 ? this.config.partyConfig().defaultMemberLimit() : result;
    }

    @Override
    protected @NotNull String playerName(final @NotNull ProxiedPlayer player) {
        return player.getName();
    }

    @Override
    protected @NotNull UUID playerUUID(final @NotNull ProxiedPlayer player) {
        return player.getUniqueId();
    }

    @Override
    protected @NotNull MessageConfig messageConfig() {
        return this.config.messageConfig();
    }
}
