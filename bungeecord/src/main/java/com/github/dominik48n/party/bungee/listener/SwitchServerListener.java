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
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.user.UserManager;
import java.util.Optional;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class SwitchServerListener implements Listener {

    private final @NotNull UserManager<ProxiedPlayer> userManager;

    public SwitchServerListener(final @NotNull UserManager<ProxiedPlayer> userManager) {
        this.userManager = userManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleServerConnected(final ServerConnectedEvent event) throws JsonProcessingException {
        final PartyPlayer player = this.userManager.createOrGetPlayer(event.getPlayer());

        final Optional<Party> party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();
        if (party.isEmpty() || !party.get().isLeader(player.uniqueId())) return;

        PartyAPI.get().connectPartyToServer(party.get(), event.getServer().getInfo().getName());
    }
}
