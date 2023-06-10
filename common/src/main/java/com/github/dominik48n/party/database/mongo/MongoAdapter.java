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

package com.github.dominik48n.party.database.mongo;

import com.github.dominik48n.party.config.DatabaseConfig;
import com.github.dominik48n.party.database.DatabaseAdapter;
import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import java.util.UUID;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MongoAdapter implements DatabaseAdapter {

    private final @NotNull DatabaseConfig.MongoConfig mongoConfig;

    private @Nullable MongoDatabase mongoDatabase;
    private @Nullable MongoClient mongoClient;

    public MongoAdapter(final @NotNull DatabaseConfig.MongoConfig mongoConfig) {
        this.mongoConfig = mongoConfig;
    }

    @Override
    public void connect() {
        this.mongoClient = MongoClients.create(this.mongoConfig.uri());
        this.mongoDatabase = this.mongoClient.getDatabase(this.mongoConfig.database());
    }

    @Override
    public boolean getSettingValue(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type) {
        final FindIterable<Document> cursor = this.getCollection("settings")
                .find(Filters.eq("unique_id", uniqueId.toString()))
                .cursorType(CursorType.NonTailable);

        final Document document = cursor.first();
        return document == null || document.getBoolean(type.name(), true);
    }

    @Override
    public void toggleSetting(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type, final boolean value) {
        this.getCollection("settings").updateOne(
                Filters.eq("unique_id", uniqueId.toString()),
                new Document()
                        .append("$setOnInsert", new Document("unique_id", uniqueId.toString()))
                        .append("$set", new Document(type.name(), value)),
                new UpdateOptions().upsert(true)
        );
    }

    private @NotNull MongoCollection<Document> getCollection(final @NotNull String name) {
        if (this.mongoDatabase == null) throw new IllegalStateException("MongoDB Database isn't initialized.");
        return this.mongoDatabase.getCollection(this.mongoConfig.collectionPrefix() + name);
    }

    @Override
    public void close() {
        if (this.mongoClient == null) throw new IllegalStateException("MongoDB Client isn't initialized.");
        this.mongoClient.close();
    }
}