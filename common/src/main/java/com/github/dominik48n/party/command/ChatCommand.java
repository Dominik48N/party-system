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
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ChatCommand {

    private final @NotNull CommandManager commandManager;

    public ChatCommand(final @NotNull CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length == 0) {
            player.sendMessage("command.usage.chat");
            return;
        }

        final String message = String.join(" ", args);
        if (message.isEmpty()) {
            player.sendMessage("command.usage.chat");
            return;
        }

        this.commandManager.runAsynchronous(() -> {
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

            PartyAPI.get().sendMessageToParty(party.get(), "party.chat", player.name(), message);
        });
    }
}
