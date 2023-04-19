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

import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.user.User;
import com.github.dominik48n.party.user.UserManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class OnlinePlayersListener implements Listener {

    private final @NotNull UserManager<ProxiedPlayer> userManager;

    public OnlinePlayersListener(final @NotNull UserManager<ProxiedPlayer> userManager) {
        this.userManager = userManager;
    }

    @EventHandler
    public void handlePostLogin(final PostLoginEvent event) {
        PartyAPI.get().onlinePlayerProvider().login(new User<>(event.getPlayer(), this.userManager));
    }

    @EventHandler
    public void handlePlayerDisconnect(final PlayerDisconnectEvent event) {
        PartyAPI.get().onlinePlayerProvider().logout(event.getPlayer().getUniqueId());
        this.userManager.removePlayerFromCache(event.getPlayer());
    }
}
