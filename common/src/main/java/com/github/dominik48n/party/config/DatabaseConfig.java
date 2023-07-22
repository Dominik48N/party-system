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

import com.github.dominik48n.party.database.DatabaseType;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public record DatabaseConfig(boolean enabled, @NotNull DatabaseType type, @NotNull MongoConfig mongoConfig, @NotNull SQLConfig sqlConfig) {

    static @NotNull DatabaseConfig fromDocument(final @NotNull Document document) {
        final String typeName = document.getString("type", DatabaseType.MONGODB.name());
        return new DatabaseConfig(
                document.getBoolean("enabled", false),
                Arrays.stream(DatabaseType.values()).filter(type -> type.name().equalsIgnoreCase(typeName)).findAny().orElse(DatabaseType.UNKNOWN),
                MongoConfig.fromDocument(document.getDocument("mongodb")),
                SQLConfig.fromDocument(document.getDocument("sql"))
        );
    }

    @NotNull Document toDocument() {
        return new Document()
                .append("enabled", this.enabled)
                .append("type", this.type.name())
                .append("mongodb", this.mongoConfig.toDocument())
                .append("sql", this.sqlConfig.toDocument());
    }

    public record MongoConfig(@NotNull String uri, @NotNull String database, @NotNull String collectionPrefix) {

        private static @NotNull MongoConfig fromDocument(final @NotNull Document document) {
            return new MongoConfig(
                    document.getString("uri", "mongodb://127.0.0.1:27017"),
                    document.getString("database", "party"),
                    document.getString("collection_prefix", "party-")
            );
        }

        private @NotNull Document toDocument() {
            return new Document().append("uri", this.uri).append("database", this.database).append("collection_prefix", this.collectionPrefix);
        }
    }

    public record SQLConfig(
            @NotNull String hostname,
            int port,
            @NotNull String username,
            @NotNull String password,
            @NotNull String database,
            @NotNull String tablePrefix,
            @NotNull String postgresSchema,
            @NotNull PoolConfig poolConfig
    ) {

        private static @NotNull SQLConfig fromDocument(final @NotNull Document document) {
            return new SQLConfig(
                    document.getString("hostname", "127.0.0.1"),
                    document.getInt("port", 5432),
                    document.getString("username", "party"),
                    document.getString("password", "topsecret"),
                    document.getString("database", "party"),
                    document.getString("table_prefix", "party_"),
                    document.getString("postgres_schema", "public"),
                    PoolConfig.fromDocument(document.getDocument("pool"))
            );
        }

        private @NotNull Document toDocument() {
            return new Document()
                    .append("hostname", this.hostname)
                    .append("port", this.port)
                    .append("username", this.username)
                    .append("password", this.password)
                    .append("database", this.database)
                    .append("table_prefix", this.tablePrefix)
                    .append("postgres_schema", this.postgresSchema)
                    .append("pool", this.poolConfig.toDocument());
        }

        public record PoolConfig(int maxPoolSize, int minIdle, long connectionTimeout, long idleTimeout, long maxLife) {

            private static @NotNull PoolConfig fromDocument(final @NotNull Document document) {
                return new PoolConfig(
                        document.getInt("max_pool_size", 5),
                        document.getInt("minimum_idle", 5),
                        document.getLong("connection_timeout", 10000L),
                        document.getLong("idle_timeout", 600000L),
                        document.getLong("max_lifetime", 1800000L)
                );
            }

            private @NotNull Document toDocument() {
                return new Document()
                        .append("max_pool_size", this.maxPoolSize)
                        .append("minimum_idle", this.minIdle)
                        .append("connection_timeout", this.connectionTimeout)
                        .append("idle_timeout", this.idleTimeout)
                        .append("max_lifetime", this.maxLife);
            }
        }
    }
}
