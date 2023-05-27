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

package com.github.dominik48n.party.velocity;

import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.command.ChatCommand;
import com.github.dominik48n.party.command.CommandManager;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class VelocityCommandManager implements RawCommand {

    private final @NotNull UserManager<Player> userManager;
    private final @NotNull CommandManager commandManager;
    private final @NotNull ChatCommand chatCommand;

    public VelocityCommandManager(final @NotNull UserManager<Player> userManager, final @NotNull PartyVelocityPlugin plugin) {
        this.commandManager = new CommandManager() {
            @Override
            public void runAsynchronous(final @NotNull Runnable runnable) {
                plugin.server().getScheduler().buildTask(plugin, runnable).schedule();
            }

            @Override
            public @NotNull ProxyPluginConfig config() {
                return plugin.config();
            }

            @Override
            public @NotNull RedisManager redisManager() {
                return plugin.redisManager();
            }
        };
        this.chatCommand = new ChatCommand(this.commandManager);
        this.userManager = userManager;
    }

    @Override
    public void execute(final Invocation invocation) {
        if (!(invocation.source() instanceof final Player player)) {
            invocation.source().sendMessage(Component.text("This command is only available for players!"));
            return;
        }

        this.userManager.getPlayer(player).ifPresent(partyPlayer -> {
            if (invocation.alias().equalsIgnoreCase("p")) {
                this.chatCommand.execute(partyPlayer, invocation.arguments().split(" "));
                return;
            }

            this.commandManager.execute(partyPlayer, invocation.arguments().split(" "));
        });
        // TODO: Send message to player if the party player isn't exist in cache
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        if (invocation.alias().equalsIgnoreCase("p")) return Collections.emptyList();
        return this.commandManager.tabComplete(invocation.arguments().split(" "));
    }
}
