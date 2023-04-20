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

public class KickCommand extends PartyCommand {

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length != 1) {
            player.sendMessage("command.usage.kick");
            return;
        }

        final Optional<Party> party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();
        if (party.isEmpty()) {
            player.sendMessage("command.not_in_party");
            return;
        }

        if (!party.get().isLeader(player.uniqueId())) {
            player.sendMessage("command.kick.not_leader");
            return;
        }

        final String name = args[0];
        if (player.name().equalsIgnoreCase(name)) {
            player.sendMessage("command.kick.self");
            return;
        }

        final Optional<PartyPlayer> target = PartyAPI.get().onlinePlayerProvider().get(name);
        if (target.isEmpty() || !party.get().members().contains(target.get().uniqueId())) {
            player.sendMessage("command.not_in_your_party");
            return;
        }

        party.get().members().remove(target.get().uniqueId());
        PartyAPI.get().sendMessageToParty(party.get(), "party.kick", name);
        PartyAPI.get().removePlayerFromParty(party.get().id(), target.get().uniqueId(), name);

        target.get().sendMessage("command.kick.kicked");
        player.sendMessage("command.kick.leader", name);
    }
}
