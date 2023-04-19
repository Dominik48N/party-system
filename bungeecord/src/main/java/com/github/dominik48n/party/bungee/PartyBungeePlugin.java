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
import com.github.dominik48n.party.bungee.listener.OnlinePlayersListener;
import com.github.dominik48n.party.bungee.listener.SwitchServerListener;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.redis.RedisManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PartyBungeePlugin extends Plugin {

    private @NotNull ProxyPluginConfig config = new ProxyPluginConfig();

    private @Nullable RedisManager redisManager = null;

    @Override
    public void onEnable() {
        final File configFile = new File(this.getDataFolder(), ProxyPluginConfig.FILE_NAME);
        try {
            this.config = ProxyPluginConfig.fromFile(configFile);
        } catch (final FileNotFoundException e) {
            try {
                this.config.writeToFile(configFile);
            } catch (final IOException e1) {
                this.getLogger().log(Level.SEVERE, "Failed to write configuration file.", e);
            }
        }

        this.redisManager = new RedisManager(this.config.redisConfig(), r -> this.getProxy().getScheduler().runAsync(this, r));

        final BungeeUserManager userManager = new BungeeUserManager(this);
        new DefaultPartyProvider<>(this.redisManager, userManager, this.config.messageConfig());

        this.redisManager.subscribes(userManager);

        this.getProxy().getPluginManager().registerListener(this, new OnlinePlayersListener(userManager));
        this.getProxy().getPluginManager().registerListener(this, new SwitchServerListener(userManager));
        this.getProxy().getPluginManager().registerCommand(this, new BungeeCommandManager(userManager, this));
    }

    @Override
    public void onDisable() {
        if (this.redisManager != null) this.redisManager.close();
    }

    public @NotNull ProxyPluginConfig config() {
        return this.config;
    }

    public @NotNull RedisManager redisManager() {
        if (this.redisManager == null) throw new IllegalStateException("RedisManager isn't initialized.");
        return this.redisManager;
    }
}
