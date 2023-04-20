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
import java.util.UUID;
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
            this.deleteParty(party.get(), player);
        } else {
            if (party.get().isLeader(player.uniqueId())) {
                final Optional<UUID> target = party.get().members().stream().findAny();
                if (target.isPresent()) {
                    PartyAPI.get().changePartyLeader(party.get().id(), player.uniqueId(), target.get());
                    PartyAPI.get().sendMessageToMembers(party.get(), "party.left", player.name());

                    PartyAPI.get().onlinePlayerProvider().get(target.get()).ifPresent(
                            targetPlayer -> PartyAPI.get().sendMessageToMembers(party.get(), "party.new_leader", targetPlayer.name())
                    );
                } else this.deleteParty(party.get(), player);
            } else {
                party.get().members().remove(player.uniqueId());
                PartyAPI.get().sendMessageToParty(party.get(), "party.left", player.name());
            }

            PartyAPI.get().removePlayerFromParty(party.get().id(), player.uniqueId(), player.name());
        }

        player.partyId(null);
        player.sendMessage("command.leave");
    }

    private void deleteParty(final @NotNull Party party, final @NotNull PartyPlayer player) {
        PartyAPI.get().deleteParty(party.id());
        PartyAPI.get().onlinePlayerProvider().updatePartyId(player.uniqueId(), null);
        PartyAPI.get().clearPartyRequest(player.name());
    }
}
