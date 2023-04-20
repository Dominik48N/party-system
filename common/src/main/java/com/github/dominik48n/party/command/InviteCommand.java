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
import com.github.dominik48n.party.config.PartyConfig;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class InviteCommand extends PartyCommand {

    private final @NotNull PartyConfig config;

    InviteCommand(final @NotNull PartyConfig config) {
        this.config = config;
    }

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length != 1) {
            player.sendMessage("command.usage.invite");
            return;
        }

        String name = args[0];
        if (name.equalsIgnoreCase(player.name())) {
            player.sendMessage("command.invite.self");
            return;
        }

        final Optional<PartyPlayer> target = PartyAPI.get().onlinePlayerProvider().get(name);
        if (target.isEmpty()) {
            player.sendMessage("general.player_not_online", name);
            return;
        }

        if (target.get().partyId().isPresent()) {
            player.sendMessage("command.invite.already_in_party");
            return;
        }

        name = target.get().name();

        if (PartyAPI.get().existsPartyRequest(player.name(), name)) {
            player.sendMessage("command.invite.already_invited");
            return;
        }

        final Optional<Party> party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();

        if (party.isEmpty()) {
            final Party createdParty = PartyAPI.get().createParty(player.uniqueId());

            if (!PartyAPI.get().onlinePlayerProvider().updatePartyId(player.uniqueId(), createdParty.id())) {
                player.sendMessage("general.error");
                PartyAPI.get().deleteParty(createdParty.id());
                return;
            }

            player.partyId(createdParty.id());
            player.sendMessage("command.invite.created_party");
        } else if (!party.get().isLeader(player.uniqueId())) {
            player.sendMessage("command.invite.not_leader");
            return;
        }

        PartyAPI.get().createPartyRequest(player.name(), name, this.config.requestExpires());

        player.sendMessage("command.invite.sent", name);
        target.get().sendMessage("command.invite.received", player.name());
    }
}
