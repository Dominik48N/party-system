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

import com.github.dominik48n.party.config.ProxyPluginConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PartyBungeePlugin extends Plugin {

    private @NotNull ProxyPluginConfig config = new ProxyPluginConfig();

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

        this.getProxy().getPluginManager().registerCommand(this, new BungeeCommandManager(new BungeePlayerManager(this)));
    }

    public @NotNull ProxyPluginConfig config() {
        return this.config;
    }
}