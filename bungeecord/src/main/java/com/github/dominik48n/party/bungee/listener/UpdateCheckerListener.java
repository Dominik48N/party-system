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

package com.github.dominik48n.party.bungee.listener;

import com.github.dominik48n.party.bungee.PartyBungeePlugin;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.utils.Constants;
import com.github.dominik48n.party.utils.UpdateChecker;
import java.io.IOException;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class UpdateCheckerListener implements Listener {

    private final @NotNull PartyBungeePlugin plugin;
    private final @NotNull MessageConfig messageConfig;

    public UpdateCheckerListener(final @NotNull PartyBungeePlugin plugin, final @NotNull MessageConfig messageConfig) {
        this.plugin = plugin;
        this.messageConfig = messageConfig;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handlePostLogin(final PostLoginEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        if (!player.hasPermission(Constants.UPDATE_CHECKER_PERMISSION)) return;

        this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
            try {
                final String latestVersion = UpdateChecker.latestVersion(Constants.GITHUB_OWNER, Constants.GITHUB_REPOSITORY);
                if (latestVersion.equals(UpdateCheckerListener.this.plugin.getDescription().getVersion())) return;

                final Component message = UpdateCheckerListener.this.messageConfig.getMessage("general.updates.new_version");
                UpdateCheckerListener.this.plugin.audiences().player(player).sendMessage(message);
            } catch (final IOException | InterruptedException e) {
                UpdateCheckerListener.this.plugin.getLogger().log(Level.SEVERE, "Failed to check latest PartySystem version.", e);

                final Component message = UpdateCheckerListener.this.messageConfig.getMessage("general.updates.failed");
                UpdateCheckerListener.this.plugin.audiences().player(player).sendMessage(message);
            }
        });
    }
}
