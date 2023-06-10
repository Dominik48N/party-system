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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
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
    public void disconnect() {
        if (this.mongoClient != null) this.mongoClient.close();
    }
}
