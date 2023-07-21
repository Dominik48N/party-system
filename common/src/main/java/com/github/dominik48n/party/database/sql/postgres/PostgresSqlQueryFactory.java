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

package com.github.dominik48n.party.database.sql.postgres;

import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import com.github.dominik48n.party.database.sql.SqlQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostgresSqlQueryFactory extends SqlQueryFactory {

    PostgresSqlQueryFactory(final @Nullable DataSource dataSource, final @NotNull String tablePrefix) {
        super(dataSource, tablePrefix);
    }

    @Override
    public @NotNull List<UUID> getPlayersWithEnabledSetting(final @NotNull List<UUID> players, final @NotNull DatabaseSettingsType type) {
        if (players.isEmpty()) return players;

        final String uuidPlaceholder = String.join(",", Collections.nCopies(players.size(), "?"));
        final String settingColumn = type.name().toLowerCase();
        return super.builder(UUID.class)
                .query("SELECT unique_id, " + settingColumn + " FROM " + super.settingsTable() + " WHERE unique_id IN (" + uuidPlaceholder + ")")
                .parameter(paramBuilder -> {
                    for (final UUID player : players) {
                        paramBuilder.setString(player.toString());
                    }
                })
                .readRow(row -> {
                    if (!row.getBoolean(settingColumn)) return null;

                    try {
                        return UUID.fromString(row.getString("unique_id"));
                    } catch (final IllegalArgumentException e) {
                        return null;
                    }
                })
                .allSync()
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean getSettingValue(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type) {
        final String settingColumn = type.name().toLowerCase();
        return super.builder(Boolean.class)
                .query("SELECT " + settingColumn + " FROM " + super.settingsTable() + " WHERE unique_id = ?")
                .parameter(paramBuilder -> paramBuilder.setString(uniqueId.toString()))
                .readRow(row -> row.getBoolean(settingColumn))
                .firstSync()
                .orElse(true);
    }

    @Override
    public void toggleSetting(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type, final boolean value) {
        final String settingColumn = type.name().toLowerCase();
        super.builder()
                .query("INSERT INTO " + super.settingsTable() + " (unique_id, " + settingColumn + ") VALUES (?, ?) " +
                        "ON CONFLICT (unique_id) DO UPDATE SET " + settingColumn + " = EXCLUDED." + settingColumn)
                .parameter(paramBuilder -> {
                    paramBuilder.setString(uniqueId.toString());
                    paramBuilder.setBoolean(value);
                })
                .update()
                .sendSync();
    }
}
