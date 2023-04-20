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

import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.player.PartyPlayer;
import org.jetbrains.annotations.NotNull;

public class DenyCommand extends PartyCommand {

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length != 1) {
            player.sendMessage("command.usage.deny");
            return;
        }

        final String name = args[0];
        if (!PartyAPI.get().existsPartyRequest(name, player.name())) {
            player.sendMessage("command.deny.no_request");
            return;
        }

        PartyAPI.get().removePartyRequest(name, player.name());
        player.sendMessage("command.deny.declined");

        PartyAPI.get().onlinePlayerProvider().get(name).ifPresent(sender -> sender.sendMessage("command.deny.other", player.name()));
    }
}
