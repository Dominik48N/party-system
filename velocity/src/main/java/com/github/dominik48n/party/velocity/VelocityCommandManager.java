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

import com.github.dominik48n.party.command.ChatCommand;
import com.github.dominik48n.party.command.CommandManager;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.database.DatabaseAdapter;
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
    private final @NotNull MessageConfig messageConfig;

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
        this.messageConfig = plugin.config().messageConfig();

        if (plugin.config().databaseConfig().enabled()) {
            DatabaseAdapter.createFromConfig(plugin.config().databaseConfig()).ifPresentOrElse(
                    databaseAdapter -> {
                        plugin.databaseAdapter(databaseAdapter);

                        plugin.logger().info("Connect to " + plugin.config().databaseConfig().type().name() + "...");
                        try {
                            databaseAdapter.connect();
                            plugin.logger().info("The connection to the database has been established.");

                            VelocityCommandManager.this.commandManager.addToggleCommand(databaseAdapter);
                            VelocityCommandManager.this.chatCommand.databaseAdapter(databaseAdapter);
                        } catch (final Exception e) {
                            plugin.logger().error(
                                    "The connection to the database could not be established, which is why the party settings cannot be activated.",
                                    e
                            );
                        }
                    },
                    () -> plugin.logger().warn("An unsupported database system was specified, which is why the party settings cannot be activated.")
            );
        } else plugin.logger().warn("The database support is deactivated, which is why the settings cannot be activated.");
    }

    @Override
    public void execute(final Invocation invocation) {
        if (!(invocation.source() instanceof final Player player)) {
            invocation.source().sendMessage(Component.text("This command is only available for players!"));
            return;
        }

        this.userManager.getPlayer(player).ifPresentOrElse(partyPlayer -> {
            if (invocation.alias().equalsIgnoreCase("p")) {
                this.chatCommand.execute(partyPlayer, invocation.arguments().split(" "));
                return;
            }

            this.commandManager.execute(partyPlayer, invocation.arguments().split(" "));
        }, () -> player.sendMessage(this.messageConfig.getMessage("command.user_not_loaded")));
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        if (invocation.alias().equalsIgnoreCase("p")) return Collections.emptyList();
        return this.commandManager.tabComplete(invocation.arguments().split(" "));
    }
}
