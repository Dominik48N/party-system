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
import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.user.UserManager;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class SwitchServerListener implements Listener {

    private final @NotNull UserManager<ProxiedPlayer> userManager;
    private final @NotNull Logger logger;

    public SwitchServerListener(final @NotNull UserManager<ProxiedPlayer> userManager, final @NotNull Logger logger) {
        this.userManager = userManager;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleServerConnected(final ServerConnectedEvent event) {
        this.userManager.getPlayer(event.getPlayer()).ifPresent(player -> {
            final Optional<Party> party;
            try {
                party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();
            } catch (final JsonProcessingException e) {
                SwitchServerListener.this.logger.log(Level.SEVERE, "Failed to get party.", e);
                return;
            }
            if (party.isEmpty() || !party.get().isLeader(player.uniqueId())) return;

            PartyAPI.get().connectPartyToServer(party.get(), event.getServer().getInfo().getName());
        });
    }
}
