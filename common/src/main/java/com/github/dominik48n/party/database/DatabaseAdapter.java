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

package com.github.dominik48n.party.database;

import com.github.dominik48n.party.config.DatabaseConfig;
import com.github.dominik48n.party.database.mongo.MongoAdapter;
import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface DatabaseAdapter extends AutoCloseable {

    static @NotNull Optional<DatabaseAdapter> createFromConfig(final @NotNull DatabaseConfig config) {
        if (!config.enabled()) return Optional.empty();

        final DatabaseAdapter databaseAdapter;
        switch (config.type()) {
            case MONGODB -> databaseAdapter = new MongoAdapter(config.mongoConfig());
            default -> {
                return Optional.empty();
            }
        }
        return Optional.of(databaseAdapter);
    }

    void connect();

    boolean getSettingValue(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type);

    void toggleSetting(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type, final boolean value);
}
