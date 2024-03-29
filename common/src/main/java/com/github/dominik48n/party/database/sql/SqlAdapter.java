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

import com.github.dominik48n.party.config.DatabaseConfig;
import com.github.dominik48n.party.database.DatabaseAdapter;
import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sadu.datasource.stage.ConfigurationStage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SqlAdapter implements DatabaseAdapter {

    protected final @NotNull DatabaseConfig.SQLConfig sqlConfig;
    protected @Nullable HikariDataSource dataSource;

    private @Nullable SqlQueryFactory queryFactory;

    public SqlAdapter(final @NotNull DatabaseConfig.SQLConfig sqlConfig) {
        this.sqlConfig = sqlConfig;
    }

    @Override
    public void connect() {
        this.dataSource = this.configurationStage()
                .withMaximumPoolSize(this.sqlConfig.poolConfig().maxPoolSize())
                .withMinimumIdle(this.sqlConfig.poolConfig().minIdle())
                .withConnectionTimeout(this.sqlConfig.poolConfig().connectionTimeout())
                .withIdleTimeout(this.sqlConfig.poolConfig().idleTimeout())
                .withMaxLifetime(this.sqlConfig.poolConfig().maxLife())
                .withThreadFactory(r -> {
                    final ThreadGroup group = new ThreadGroup("Hikari Worker");
                    final Thread thread = new Thread(group, r, group.getName());
                    thread.setDaemon(true);
                    return thread;
                })
                .build();
        this.queryFactory = this.queryFactory();

        try {
            this.queryFactory.executeUpdater();
        } catch (final IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (this.dataSource != null) this.dataSource.close();
    }

    @Override
    public @NotNull List<UUID> getPlayersWithEnabledSetting(final @NotNull List<UUID> players, final @NotNull DatabaseSettingsType type) {
        return this.queryFactory != null ? this.queryFactory.getPlayersWithEnabledSetting(players, type) : players;
    }

    @Override
    public boolean getSettingValue(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type) {
        return this.queryFactory == null || this.queryFactory.getSettingValue(uniqueId, type);
    }

    @Override
    public void toggleSetting(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type, final boolean value) {
        if (this.queryFactory != null) this.queryFactory.toggleSetting(uniqueId, type, value);
    }

    protected abstract @NotNull SqlQueryFactory queryFactory();

    protected abstract @NotNull ConfigurationStage configurationStage();
}
