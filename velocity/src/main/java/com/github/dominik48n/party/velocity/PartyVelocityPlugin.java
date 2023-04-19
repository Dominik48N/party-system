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

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Plugin(
        id = "party-system",
        name = "PartySystem",
        version = "@version@",
        url = "https://github.com/Dominik48N/party-system",
        description = "Allow players to play together in rounds with this plugin.",
        authors = {"Dominik48N"}
)
public class PartyVelocityPlugin {

    private static @Nullable PartyVelocityPlugin instance;

    public static @NotNull PartyVelocityPlugin get() throws IllegalStateException {
        if (instance == null) throw new IllegalStateException("Party Plugin isn't initialized.");
        return instance;
    }

    private final @NotNull ProxyServer server;
    private final @NotNull Logger logger;

    @Inject
    public PartyVelocityPlugin(final @NotNull ProxyServer server, final @NotNull Logger logger) {
        this.server = server;
        this.logger = logger;

        instance = this;
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        this.server.getCommandManager().register(
                this.server.getCommandManager().metaBuilder("party").plugin(this).build(),
                new VelocityCommandManager(new VelocityPlayerManager())
        );
    }

    public @NotNull ProxyServer server() {
        return this.server;
    }

    public @NotNull Logger logger() {
        return this.logger;
    }
}
