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

import com.github.dominik48n.party.api.DefaultPartyProvider;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.bungee.command.PartyChatCommand;
import com.github.dominik48n.party.bungee.listener.OnlinePlayersListener;
import com.github.dominik48n.party.bungee.listener.SwitchServerListener;
import com.github.dominik48n.party.bungee.listener.UpdateCheckerListener;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.database.DatabaseAdapter;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.util.Constants;
import com.github.dominik48n.party.util.UpdateChecker;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PartyBungeePlugin extends Plugin {

    private @NotNull ProxyPluginConfig config = new ProxyPluginConfig();

    private @Nullable DatabaseAdapter databaseAdapter = null;
    private @Nullable RedisManager redisManager = null;
    private @Nullable BungeeAudiences audiences = null;

    private @Nullable DefaultPartyProvider<ProxiedPlayer> partyProvider;

    @Override
    public void onEnable() {
        final File configFile = new File(this.getDataFolder(), ProxyPluginConfig.FILE_NAME);
        this.getDataFolder().mkdirs();
        try {
            this.config = ProxyPluginConfig.fromFile(configFile);
        } catch (final IOException e) {
            try {
                this.config = new ProxyPluginConfig();
                this.config.writeToFile(configFile);
            } catch (final IOException e1) {
                this.getLogger().log(Level.SEVERE, "Failed to write configuration file.", e);
            }
        }

        try {
            this.redisManager = new RedisManager(this.config.redisConfig());
            this.getLogger().info("The connection to Redis has been established.");
        } catch (final Exception e) {
            this.getLogger().log(
                    Level.SEVERE,
                    "The connection to redis could not be established. The party system is therefore not fully loaded.",
                    e
            );
            // If the code continued, there would be some problems at runtime, which is
            // why the plugin simply does not load completely when the error occurs here.
            return;
        }

        this.audiences = BungeeAudiences.create(this);

        final BungeeUserManager userManager = new BungeeUserManager(this);
        this.partyProvider = new DefaultPartyProvider<>(this.redisManager, userManager, this.config.messageConfig());

        this.redisManager.subscribes(userManager);

        final BungeeCommandManager bungeeCommandManager = new BungeeCommandManager(userManager, this);

        this.getProxy().getPluginManager().registerListener(this, new OnlinePlayersListener(userManager, this));
        this.getProxy().getPluginManager().registerListener(this, new SwitchServerListener(userManager, this.getLogger()));

        this.getProxy().getPluginManager().registerCommand(
                this,
                new PartyChatCommand(bungeeCommandManager.commandManager, userManager, this.config().messageConfig())
        );
        this.getProxy().getPluginManager().registerCommand(this, bungeeCommandManager);

        if (this.config.updateChecker()) this.registerUpdateChecker();
    }

    /**
     * Registers the update checker for the PartyBungeePlugin.
     * The update checker checks for the latest version of the party system and logs a message if a new version is available.
     * It also registers an UpdateCheckerListener to notify admins when they join about the availability of a new version.
     */
    private void registerUpdateChecker() {
        final String currentVersion = this.getDescription().getVersion();
        this.getProxy().getScheduler().schedule(this, () -> {
            try {
                final String latestVersion = UpdateChecker.latestVersion(Constants.GITHUB_OWNER, Constants.GITHUB_REPOSITORY);
                if (latestVersion.equals(currentVersion)) return; // Up to date :)

                PartyBungeePlugin.this.getLogger().log(Level.INFO, "There is a new version of the party system: {0}", new Object[] {latestVersion});
            } catch (final IOException | InterruptedException e) {
                PartyBungeePlugin.this.getLogger().log(Level.SEVERE, "Failed to check latest PartySystem version.", e);
            }
        }, 1L, 24L * 60L * 60L, TimeUnit.SECONDS); // Call 1 second after start and daily thereafter.

        this.getProxy().getPluginManager().registerListener(this, new UpdateCheckerListener(this, this.config().messageConfig()));
    }

    @Override
    public void onDisable() {
        for (final ProxiedPlayer player : this.getProxy().getPlayers()) {
            PartyAPI.get().onlinePlayerProvider().logout(player.getUniqueId());
            PartyAPI.get().clearPartyRequest(player.getName());
        }

        if (this.redisManager != null) {
            this.getLogger().info("Close connection to redis...");
            this.redisManager.close();
        } else this.getLogger().warning("The connection to redis is not closed, because the redis manager is not initialized.");

        if (this.databaseAdapter != null) {
            this.getLogger().info("Closing connection to database...");
            try {
                this.databaseAdapter.close();
                this.getLogger().info("Closed database connection!");
            } catch (final Exception e) {
                this.getLogger().log(Level.SEVERE, "Failed to close database connection.", e);
            }
        }
    }

    public @NotNull ProxyPluginConfig config() {
        return this.config;
    }

    public @NotNull RedisManager redisManager() {
        if (this.redisManager == null) throw new IllegalStateException("RedisManager isn't initialized.");
        return this.redisManager;
    }

    public @NotNull BungeeAudiences audiences() {
        if (this.audiences == null) throw new IllegalStateException("Audience isn't initialized.");
        return this.audiences;
    }

    void databaseAdapter(final @NotNull DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
        if (this.partyProvider != null) this.partyProvider.databaseAdapter(databaseAdapter);
    }
}
