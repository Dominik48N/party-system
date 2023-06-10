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

package com.github.dominik48n.party.config;

import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class ProxyPluginConfig {

    public static final @NotNull String FILE_NAME = "config.json";

    public static @NotNull ProxyPluginConfig fromFile(final @NotNull File file) throws IOException {
        final Document document = Document.read(file);
        return new ProxyPluginConfig(document);
    }

    private final @NotNull RedisConfig redisConfig;
    private final @NotNull DatabaseConfig databaseConfig;
    private final @NotNull PartyConfig partyConfig;
    private final @NotNull MessageConfig messageConfig;

    private final boolean updateChecker;

    public ProxyPluginConfig() {
        this(new Document());
    }

    private ProxyPluginConfig(final @NotNull Document document) {
        this.redisConfig = RedisConfig.fromDocument(document.getDocument("redis"));
        this.databaseConfig = DatabaseConfig.fromDocument(document.getDocument("database"));
        this.partyConfig = PartyConfig.fromDocument(document.getDocument("party"));
        this.messageConfig = MessageConfig.fromDocument(document.getDocument("messages"));

        this.updateChecker = document.getBoolean("update_checker", true);
    }

    public @NotNull RedisConfig redisConfig() {
        return this.redisConfig;
    }

    public @NotNull MessageConfig messageConfig() {
        return this.messageConfig;
    }

    public @NotNull PartyConfig partyConfig() {
        return this.partyConfig;
    }

    public @NotNull DatabaseConfig databaseConfig() {
        return this.databaseConfig;
    }

    public boolean updateChecker() {
        return this.updateChecker;
    }

    public void writeToFile(final @NotNull File file) throws IOException {
        new Document()
                .append("update_checker", this.updateChecker)
                .append("redis", this.redisConfig.toDocument())
                .append("database", this.databaseConfig.toDocument())
                .append("party", this.partyConfig.toDocument())
                .append("messages", this.messageConfig.toDocument())
                .writeToFile(file);
    }
}
