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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class ListCommand extends PartyCommand {

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        Optional<Party> party;
        try {
            party = player.partyId().isPresent() ? PartyAPI.get().getParty(player.partyId().get()) : Optional.empty();
        } catch (final JsonProcessingException e) {
            party = Optional.empty();
        }
        if (party.isEmpty()) {
            player.sendMessage("command.not_in_party");
            return;
        }

        Map<UUID, PartyPlayer> players;
        try {
            players = PartyAPI.get().onlinePlayerProvider().get(party.get().getAllMembers());
        } catch (JsonProcessingException e) {
            players = Maps.newHashMap();
        }
        if (players.isEmpty()) return; // Weird party with empty online players

        final PartyPlayer leader = players.get(party.get().leader());
        final Party finalParty = party.get();
        final List<String> members = players.entrySet().stream()
                .filter(entry -> finalParty.members().contains(entry.getKey()))
                .map(entry -> entry.getValue().name())
                .toList();

        player.sendMessage(
                "command.list",
                leader != null ? leader.name() : '-', members.isEmpty() ? '-' : String.join("<dark_gray>, </dark_gray>", members)
        );
    }
}
