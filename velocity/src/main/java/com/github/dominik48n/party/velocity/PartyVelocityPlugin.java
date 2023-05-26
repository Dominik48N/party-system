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

import com.github.dominik48n.party.api.DefaultPartyProvider;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.util.UpdateChecker;
import com.github.dominik48n.party.velocity.listener.OnlinePlayersListener;
import com.github.dominik48n.party.velocity.listener.SwitchServerListener;
import com.github.dominik48n.party.velocity.listener.UpdateCheckerListener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
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

    private final @NotNull ProxyServer server;
    private final @NotNull Logger logger;
    private final @NotNull Path dataFolder;

    private @NotNull ProxyPluginConfig config;
    private @Nullable RedisManager redisManager;

    @Inject
    public PartyVelocityPlugin(final @NotNull ProxyServer server, final @NotNull Logger logger, final @NotNull @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;

        this.config = new ProxyPluginConfig();
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        final File configFile = new File(this.dataFolder.toFile(), ProxyPluginConfig.FILE_NAME);
        this.dataFolder.toFile().mkdirs();
        try {
            this.config = ProxyPluginConfig.fromFile(configFile);
        } catch (final IOException e) {
            try {
                this.config = new ProxyPluginConfig();
                this.config.writeToFile(configFile);
            } catch (final IOException e1) {
                this.logger.error("Failed to write configuration file.", e);
            }
        }

        try {
            this.redisManager = new RedisManager(this.config.redisConfig());
            this.logger.info("The connection to Redis has been established.");
        } catch (final Exception e) {
            this.logger.error("The connection to redis could not be established. The party system is therefore not fully loaded.", e);
            // If the code continued, there would be some problems at runtime, which is
            // why the plugin simply does not load completely when the error occurs here.
            return;
        }

        final VelocityUserManager userManager = new VelocityUserManager(this.redisManager, this.config, this.server, this.logger);
        new DefaultPartyProvider<>(this.redisManager, userManager, this.config.messageConfig());

        this.redisManager.subscribes(userManager);

        this.server.getEventManager().register(this, new OnlinePlayersListener(userManager));
        this.server.getEventManager().register(this, new SwitchServerListener(userManager));
        this.server.getCommandManager().register(
                this.server.getCommandManager().metaBuilder("party").aliases("p").plugin(this).build(),
                new VelocityCommandManager(userManager, this)
        );

        if (this.config.updateChecker()) this.registerUpdateChecker();
    }

    private void registerUpdateChecker() {
        final String currentVersion = "@version@";
        this.server.getScheduler().buildTask(this, () -> {
            try {
                final String latestVersion = UpdateChecker.latestVersion(UpdateChecker.OWNER, UpdateChecker.REPOSITORY);
                if (latestVersion.equals(currentVersion)) return; // Up to date :)

                PartyVelocityPlugin.this.logger.info("There is a new version of the party system: {}", latestVersion);
            } catch (final IOException | InterruptedException e) {
                PartyVelocityPlugin.this.logger.error("Failed to check latest PartySystem version.", e);
            }
        }).delay(1L, TimeUnit.SECONDS).repeat(24L, TimeUnit.HOURS).schedule();

        this.server.getEventManager().register(
                this,
                new UpdateCheckerListener(this, currentVersion, this.config.messageConfig(), this.logger)
        );
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        for (final Player player : this.server.getAllPlayers()) {
            PartyAPI.get().onlinePlayerProvider().logout(player.getUniqueId());
            PartyAPI.get().clearPartyRequest(player.getUsername());
        }

        if (this.redisManager != null) {
            this.logger.info("Close connection to redis...");
            this.redisManager.close();
        } else this.logger.warn("The connection to redis is not closed, because the redis manager is not initialized.");
    }

    public @NotNull ProxyPluginConfig config() {
        return this.config;
    }

    public @NotNull ProxyServer server() {
        return this.server;
    }

    public @NotNull Logger logger() {
        return this.logger;
    }

    @NotNull RedisManager redisManager() {
        if (this.redisManager == null) throw new IllegalStateException("RedisManager isn't initialized.");
        return this.redisManager;
    }
}
