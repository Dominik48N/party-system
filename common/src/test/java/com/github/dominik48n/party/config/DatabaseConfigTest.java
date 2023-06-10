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

    private final String uri = "mongodb://0.0.0.0:27018", database = "partysystem", collectionPrefix = "";

    @Test
    void testFromDocument() {
        final Document document = new Document()
                .append("enabled", true)
                .append("type", DatabaseType.POSTGRE_SQL.name())
                .append("mongodb", new Document()
                        .append("uri", this.uri)
                        .append("database", this.database)
                        .append("collection_prefix", this.collectionPrefix)
                );
        final DatabaseConfig config = DatabaseConfig.fromDocument(document);

        assertTrue(config.enabled());
        assertEquals(config.type().name(), DatabaseType.POSTGRE_SQL.name());

        assertEquals(config.mongoConfig().uri(), this.uri);
        assertEquals(config.mongoConfig().database(), this.database);
        assertEquals(config.mongoConfig().collectionPrefix(), this.collectionPrefix);
    }

    @Test
    void testToDocument() {
        final DatabaseConfig config = new DatabaseConfig(true, DatabaseType.POSTGRE_SQL, new DatabaseConfig.MongoConfig(
                this.uri,
                this.database,
                this.collectionPrefix
        ));
        final Document document = config.toDocument();

        assertTrue(document.getBoolean("enabled", false));
        assertEquals(DatabaseType.POSTGRE_SQL.name(), document.getString("type", DatabaseType.UNKNOWN.name()));

        final Document mongoDocument = document.getDocument("mongodb");
        assertEquals(mongoDocument.getString("uri", "no_value"), this.uri);
        assertEquals(mongoDocument.getString("database", "no_value"), this.database);
        assertEquals(mongoDocument.getString("collection_prefix", "no_value"), this.collectionPrefix);
    }
}
