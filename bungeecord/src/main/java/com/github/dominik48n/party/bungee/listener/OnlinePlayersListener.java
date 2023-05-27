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

package com.github.dominik48n.party.bungee.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.user.User;
import com.github.dominik48n.party.user.UserManager;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class OnlinePlayersListener implements Listener {

    private final @NotNull UserManager<ProxiedPlayer> userManager;
    private final @NotNull Plugin plugin;

    public OnlinePlayersListener(final @NotNull UserManager<ProxiedPlayer> userManager, final @NotNull Plugin plugin) {
        this.userManager = userManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePostLogin(final PostLoginEvent event) {
        // The login is executed asynchronously to the login thread, since
        // some queries are made during the login, which could take a little
        // longer depending on what other plugins are on the server. Therefore,
        // this is done asynchronously so as not to burden the login process.
        this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
            final User<ProxiedPlayer> user = new User<>(event.getPlayer(), OnlinePlayersListener.this.userManager);
            try {
                PartyAPI.get().onlinePlayerProvider().login(user);
            } catch (final JsonProcessingException e) {
                OnlinePlayersListener.this.plugin.getLogger().log(
                        Level.SEVERE,
                        "Failed to login player " + event.getPlayer().getUniqueId() + ".",
                        e
                );
            }
        });
    }

    @EventHandler
    public void handlePlayerDisconnect(final PlayerDisconnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        PartyAPI.get().onlinePlayerProvider().logout(player.getUniqueId());
        PartyAPI.get().clearPartyRequest(player.getName());

        this.userManager.removePlayerFromCache(player);
    }
}
