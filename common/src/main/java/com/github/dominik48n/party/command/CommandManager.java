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
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class CommandManager {

    private final @NotNull Map<String, PartyCommand> commands = ImmutableBiMap.of(
            "invite", new InviteCommand(this.config().partyConfig()),
            "accept", new AcceptCommand(),
            "deny", new DenyCommand(),
            "list", new ListCommand(this.config().partyConfig()),
            "leave", new LeaveCommand(),
            "promote", new PromoteCommand(),
            "kick", new KickCommand(this.redisManager())
    );

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
        this.runAsynchronous(() -> command.execute(player, commandArgs));
    }

    public @NotNull List<String> tabComplete(final @NotNull String[] args) {
        if (args.length != 1) return Lists.newArrayList();
        final String search = args[0].toLowerCase();
        return this.commands.keySet().stream().filter(s -> s.startsWith(search)).toList();
    }

    public abstract void runAsynchronous(final @NotNull Runnable runnable);

    public abstract @NotNull ProxyPluginConfig config();

    public abstract @NotNull RedisManager redisManager();
}
