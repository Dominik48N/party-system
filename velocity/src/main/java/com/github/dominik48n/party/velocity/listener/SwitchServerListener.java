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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.config.SwitchServerConfig;
import com.github.dominik48n.party.server.SwitchServer;
import com.github.dominik48n.party.user.UserManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class SwitchServerListener extends SwitchServer<Player> {

    private final @NotNull Logger logger;

    public SwitchServerListener(final @NotNull UserManager<Player> userManager,
                                final @NotNull SwitchServerConfig switchServerConfig,
                                final @NotNull Logger logger) {
        super(userManager, switchServerConfig);
        this.logger = logger;
    }

    @Subscribe(order = PostOrder.LATE)
    public void handleServerConnected(final ServerConnectedEvent event) {
        handleServerConnected(event.getPlayer(), event.getServer().getServerInfo().getName());
    }

    @Override
    public void logJsonProcessingException(JsonProcessingException jsonProcessingException) {
        this.logger.error("Failed to get party.", jsonProcessingException);
    }
}
