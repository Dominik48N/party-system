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
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DatabaseConfigTest {

    private final String uri = "mongodb://0.0.0.0:27018",
            database = "partysystem",
            collectionPrefix = "",
            hostname = "0.0.0.0",
            username = "partysystem",
            password = "github.com/Dominik48N/party-system",
            tablePrefix = "test_";
    private final int port = 3306, maxPoolSize = 10, minIdle = 8;
    private final long connectionTimeOut = 8000L, idleTimeout = 300000L, maxLifetime = 1500000L;

    @Test
    void testFromDocument() {
        final Document document = new Document()
                .append("enabled", true)
                .append("type", DatabaseType.POSTGRE_SQL.name())
                .append("mongodb", new Document()
                        .append("uri", this.uri)
                        .append("database", this.database)
                        .append("collection_prefix", this.collectionPrefix))
                .append("sql", new Document()
                        .append("hostname", this.hostname)
                        .append("port", this.port)
                        .append("username", this.username)
                        .append("password", this.password)
                        .append("database", this.database)
                        .append("table_prefix", this.tablePrefix)
                        .append("pool", new Document()
                                .append("max_pool_size", this.maxPoolSize)
                                .append("minimum_idle", this.minIdle)
                                .append("connection_timeout", this.connectionTimeOut)
                                .append("idle_timeout", this.idleTimeout)
                                .append("max_lifetime", this.maxLifetime)
                        )
                );
        final DatabaseConfig config = DatabaseConfig.fromDocument(document);

        assertTrue(config.enabled());
        assertEquals(config.type().name(), DatabaseType.POSTGRE_SQL.name());

        assertEquals(config.mongoConfig().uri(), this.uri);
        assertEquals(config.mongoConfig().database(), this.database);
        assertEquals(config.mongoConfig().collectionPrefix(), this.collectionPrefix);

        assertEquals(config.sqlConfig().hostname(), this.hostname);
        assertEquals(config.sqlConfig().port(), this.port);
        assertEquals(config.sqlConfig().username(), this.username);
        assertEquals(config.sqlConfig().password(), this.password);
        assertEquals(config.sqlConfig().database(), this.database);
        assertEquals(config.sqlConfig().tablePrefix(), this.tablePrefix);

        assertEquals(config.sqlConfig().poolConfig().maxPoolSize(), this.maxPoolSize);
        assertEquals(config.sqlConfig().poolConfig().minIdle(), this.minIdle);
        assertEquals(config.sqlConfig().poolConfig().connectionTimeout(), this.connectionTimeOut);
        assertEquals(config.sqlConfig().poolConfig().idleTimeout(), this.idleTimeout);
        assertEquals(config.sqlConfig().poolConfig().maxLife(), this.maxLifetime);
    }

    @Test
    void testToDocument() {
        final DatabaseConfig config = new DatabaseConfig(true, DatabaseType.POSTGRE_SQL, new DatabaseConfig.MongoConfig(
                this.uri,
                this.database,
                this.collectionPrefix
        ), new DatabaseConfig.SQLConfig(
                this.hostname,
                this.port,
                this.username,
                this.password,
                this.database,
                this.tablePrefix,
                new DatabaseConfig.SQLConfig.PoolConfig(
                        this.maxPoolSize,
                        this.minIdle,
                        this.connectionTimeOut,
                        this.idleTimeout,
                        this.maxLifetime
                )
        ));
        final Document document = config.toDocument();

        assertTrue(document.getBoolean("enabled", false));
        assertEquals(DatabaseType.POSTGRE_SQL.name(), document.getString("type", DatabaseType.UNKNOWN.name()));

        final Document mongoDocument = document.getDocument("mongodb");
        assertEquals(mongoDocument.getString("uri", "no_value"), this.uri);
        assertEquals(mongoDocument.getString("database", "no_value"), this.database);
        assertEquals(mongoDocument.getString("collection_prefix", "no_value"), this.collectionPrefix);

        final Document sqlDocument = document.getDocument("sql");
        assertEquals(sqlDocument.getString("hostname", "no_value"), this.hostname);
        assertEquals(sqlDocument.getInt("port", 12345), this.port);
        assertEquals(sqlDocument.getString("username", "no_value"), this.username);
        assertEquals(sqlDocument.getString("password", "no_value"), this.password);
        assertEquals(sqlDocument.getString("database", "no_value"), this.database);
        assertEquals(sqlDocument.getString("table_prefix", "no_value"), this.tablePrefix);

        final Document poolDocument = sqlDocument.getDocument("pool");
        assertEquals(poolDocument.getInt("max_pool_size", 3), this.maxPoolSize);
        assertEquals(poolDocument.getInt("minimum_idle", 6), this.minIdle);
        assertEquals(poolDocument.getLong("connection_timeout", 5L), this.connectionTimeOut);
        assertEquals(poolDocument.getLong("idle_timeout", 3131L), this.idleTimeout);
        assertEquals(poolDocument.getLong("max_lifetime", 9913L), this.maxLifetime);
    }
}
