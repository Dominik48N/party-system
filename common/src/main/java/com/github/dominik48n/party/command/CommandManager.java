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

import com.github.dominik48n.party.api.player.PartyPlayer;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class CommandManager {

    private final @NotNull Map<String, PartyCommand> commands = ImmutableBiMap.of();

    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length == 0) {
            player.sendMessage("command.help");
            return;
        }

        final PartyCommand command = this.commands.get(args[0].toLowerCase());
        if (command == null) {
            player.sendMessage("command.help");
            return;
        }

        final String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        command.execute(player, commandArgs);
    }

    public @NotNull List<String> tabComplete(final @NotNull String[] args) {
        if (args.length != 1) return Lists.newArrayList();
        final String search = args[0].toLowerCase();
        return this.commands.keySet().stream().filter(s -> s.startsWith(search)).toList();
    }
}
