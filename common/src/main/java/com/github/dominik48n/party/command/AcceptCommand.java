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

package com.github.dominik48n.party.command;

import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.player.PartyPlayer;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class AcceptCommand extends PartyCommand {

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length != 1) {
            player.sendMessage("command.usage.accept");
            return;
        }

        if (player.partyId().isPresent()) {
            player.sendMessage("command.accept.already");
            return;
        }

        final String name = args[0];
        if (!PartyAPI.get().existsPartyRequest(name, player.name())) {
            player.sendMessage("command.accept.no_request");
            return;
        }

        PartyAPI.get().removePartyRequest(name, player.name());

        final Optional<PartyPlayer> target = PartyAPI.get().onlinePlayerProvider().get(name);
        if (target.isEmpty() || target.get().partyId().isEmpty()) {
            player.sendMessage("command.accept.no_request");
            return;
        }

        final Optional<Party> party = PartyAPI.get().getParty(target.get().partyId().get());
        if (party.isEmpty()) {
            player.sendMessage("command.accept.no_request");
            return;
        }

        PartyAPI.get().sendMessageToParty(party.get(), "party.join", player.name());
        PartyAPI.get().addPlayerToParty(party.get().id(), player.uniqueId());

        player.partyId(party.get().id());
        player.sendMessage("command.accept.joined");
    }
}
