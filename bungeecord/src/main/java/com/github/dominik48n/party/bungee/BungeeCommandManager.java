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

package com.github.dominik48n.party.bungee;

import com.github.dominik48n.party.command.CommandManager;
import com.github.dominik48n.party.user.UserManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class BungeeCommandManager extends Command implements TabExecutor {

    private final @NotNull UserManager<ProxiedPlayer> userManager;
    private final @NotNull CommandManager commandManager;

    public BungeeCommandManager(final @NotNull UserManager<ProxiedPlayer> userManager) {
        super("party");
        this.commandManager = new CommandManager();
        this.userManager = userManager;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final ProxiedPlayer player)) {
            sender.sendMessage(TextComponent.fromLegacyText("This command is only available for players!"));
            return;
        }

        this.commandManager.execute(this.userManager.createPlayer(player), args);
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        return this.commandManager.tabComplete(args);
    }
}
