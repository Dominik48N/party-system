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

public class LeaveCommand extends PartyCommand {

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        final Optional<Party> party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();
        if (party.isEmpty()) {
            player.sendMessage("command.not_in_party");
            return;
        }

        if (party.get().getAllMembers().size() <= 1) {
            PartyAPI.get().deleteParty(party.get().id());
            PartyAPI.get().onlinePlayerProvider().updatePartyId(player.uniqueId(), null);
            PartyAPI.get().clearPartyRequest(player.name());
        } else {
            if (party.get().leader().equals(player.uniqueId())) {
                PartyAPI.get().onlinePlayerProvider().updatePartyId(player.uniqueId(), null);
                PartyAPI.get().clearPartyRequest(player.name());
                PartyAPI.get().sendMessageToMembers(party.get(), "party.left", player.name());

                // TODO: New party leader
            } else {
                party.get().members().remove(player.uniqueId());
                PartyAPI.get().removePlayerFromParty(party.get().id(), player.uniqueId(), player.name());
                PartyAPI.get().sendMessageToParty(party.get(), "party.left", player.name());
            }
        }

        player.sendMessage("command.leave");
    }
}
