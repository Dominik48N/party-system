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

package com.github.dominik48n.party.velocity.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.user.UserManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class SwitchServerListener {

    private final @NotNull UserManager<Player> userManager;
    private final @NotNull Logger logger;

    public SwitchServerListener(final @NotNull UserManager<Player> userManager, final @NotNull Logger logger) {
        this.userManager = userManager;
        this.logger = logger;
    }

    @Subscribe(order = PostOrder.LATE)
    public void handleServerConnected(final ServerConnectedEvent event) throws JsonProcessingException {
        this.userManager.getPlayer(event.getPlayer()).ifPresent(player -> {
            final Optional<Party> party;
            try {
                party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();
            } catch (final JsonProcessingException e) {
                SwitchServerListener.this.logger.error("Failed to get party.", e);
                return;
            }
            if (party.isEmpty() || !party.get().isLeader(player.uniqueId())) return;

            PartyAPI.get().connectPartyToServer(party.get(), event.getServer().getServerInfo().getName());
        });
    }
}
