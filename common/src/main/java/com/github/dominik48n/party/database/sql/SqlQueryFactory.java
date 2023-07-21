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

package com.github.dominik48n.party.database.sql;

import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import de.chojo.sadu.base.QueryFactory;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SqlQueryFactory extends QueryFactory {

    private final @NotNull String tablePrefix;

    public SqlQueryFactory(final @Nullable DataSource dataSource, final @NotNull String tablePrefix) {
        super(dataSource);
        this.tablePrefix = tablePrefix;
    }

    public abstract void createSettingsTable();

    public abstract @NotNull List<UUID> getPlayersWithEnabledSetting(final @NotNull List<UUID> players, final @NotNull DatabaseSettingsType type);

    public abstract boolean getSettingValue(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type);

    public abstract void toggleSetting(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type, final boolean value);

    public @NotNull String settingsTable() {
        return this.tablePrefix + "settings";
    }
}
