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

package com.github.dominik48n.party.velocity.listener;

import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.util.UpdateChecker;
import com.github.dominik48n.party.velocity.PartyVelocityPlugin;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class UpdateCheckerListener {

    private final @NotNull PartyVelocityPlugin plugin;
    private final @NotNull String currentVersion;
    private final @NotNull MessageConfig messageConfig;
    private final @NotNull Logger logger;

    public UpdateCheckerListener(
            final @NotNull PartyVelocityPlugin plugin,
            final @NotNull String currentVersion,
            final @NotNull MessageConfig messageConfig,
            final @NotNull Logger logger
    ) {
        this.plugin = plugin;
        this.currentVersion = currentVersion;
        this.messageConfig = messageConfig;
        this.logger = logger;
    }

    @Subscribe(order = PostOrder.LAST)
    public void handlePostLogin(final PostLoginEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPermission(UpdateChecker.PERMISSION)) return;

        this.plugin.server().getScheduler().buildTask(this.plugin, () -> {
            try {
                final String latestVersion = UpdateChecker.latestVersion(UpdateChecker.OWNER, UpdateChecker.REPOSITORY);
                if (latestVersion.equals(UpdateCheckerListener.this.currentVersion)) return;

                player.sendMessage(UpdateCheckerListener.this.messageConfig.getMessage("general.updates.new_version"));
            } catch (final IOException | InterruptedException e) {
                UpdateCheckerListener.this.logger.error("Failed to check latest PartySystem version.", e);
                player.sendMessage(UpdateCheckerListener.this.messageConfig.getMessage("general.updates.failed"));
            }
        }).schedule();
    }
}
