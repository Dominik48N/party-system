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
import com.github.dominik48n.party.database.DatabaseType;
import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SqlAdapter implements DatabaseAdapter {

    private final @NotNull DatabaseConfig.SQLConfig sqlConfig;
    private final @NotNull DatabaseType databaseType;

    private @Nullable HikariDataSource dataSource;
    private @Nullable Connection connection;

    public SqlAdapter(final @NotNull DatabaseConfig.SQLConfig sqlConfig, final @NotNull DatabaseType databaseType) {
        this.sqlConfig = sqlConfig;
        this.databaseType = databaseType;
    }

    @Override
    public void connect() {
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl(String.format(
                "jdbc:%s://%s:%s/%s",
                this.databaseType == DatabaseType.POSTGRESQL ? "postgresql" : this.databaseType == DatabaseType.MARIADB ? "mariadb" : "mysql",
                this.sqlConfig.hostname(),
                this.sqlConfig.port(),
                this.sqlConfig.database()
        ));
        config.setUsername(this.sqlConfig.username());
        config.setPassword(this.sqlConfig.password());

        config.setMaximumPoolSize(this.sqlConfig.poolConfig().maxPoolSize());
        config.setMinimumIdle(this.sqlConfig.poolConfig().minIdle());
        config.setConnectionTimeout(this.sqlConfig.poolConfig().connectionTimeout());
        config.setIdleTimeout(this.sqlConfig.poolConfig().idleTimeout());
        config.setMaxLifetime(this.sqlConfig.poolConfig().maxLife());

        config.setDriverClassName(switch (this.databaseType) {
            case POSTGRESQL -> "org.postgresql.Driver";
            case MARIADB -> "org.mariadb.jdbc.Driver";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            default -> "";
        });

        this.dataSource = new HikariDataSource(config);
        try {
            this.connection = this.dataSource.getConnection();

            final StringBuilder tableCreateCommand = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                    .append(this.settingsTable())
                    .append("(unique_id VARCHAR(36) NOT NULL PRIMARY KEY");

            if (this.databaseType == DatabaseType.POSTGRESQL) {
                for (final DatabaseSettingsType settingsType : DatabaseSettingsType.values()) {
                    tableCreateCommand.append(",").append(settingsType.name().toLowerCase()).append(" BOOLEAN NOT NULL DEFAULT true");
                }
            } else if (this.databaseType == DatabaseType.MARIADB || this.databaseType == DatabaseType.MYSQL) {
                for (final DatabaseSettingsType settingsType : DatabaseSettingsType.values()) {
                    tableCreateCommand.append(",").append(settingsType.name().toLowerCase()).append(" INT(1) NOT NULL DEFAULT '0'");
                }
            }

            tableCreateCommand.append(");");

            if (this.connection != null) try (final PreparedStatement statement = this.connection.prepareStatement(tableCreateCommand.toString())) {
                statement.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull List<UUID> getPlayersWithEnabledSetting(final @NotNull List<UUID> players, final @NotNull DatabaseSettingsType type) {
        if (this.connection == null) return players;

        final List<UUID> uuids = new ArrayList<>();
        final String settingColumn = type.name().toLowerCase();
        try (final PreparedStatement statement = this.connection.prepareStatement(
                "SELECT unique_id, " + settingColumn + " FROM " + this.settingsTable() + " WHERE unique_id IN (?)"
        )) {
            statement.setString(1, players.stream().map(UUID::toString).collect(Collectors.joining(",")));

            try (final ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    boolean disabled = false;
                    if (this.databaseType == DatabaseType.POSTGRESQL && !resultSet.getBoolean(settingColumn)) disabled = true;
                    else if ((this.databaseType == DatabaseType.MYSQL || this.databaseType == DatabaseType.MARIADB) && resultSet.getInt(settingColumn) == 1)
                        disabled = true;

                    if (disabled) continue;

                    final UUID uniqueId;
                    try {
                        uniqueId = UUID.fromString(resultSet.getString("unique_id"));
                    } catch (final IllegalArgumentException ignored) {
                        // This case only occurs if server administrators have manually inserted incorrect entries in the table.
                        // We ignore the incorrect entries so that the runtime is not disturbed.
                        continue;
                    }
                    uuids.add(uniqueId);
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        return uuids;
    }

    @Override
    public boolean getSettingValue(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type) {
        if (this.connection == null) return true;

        final String settingColumn = type.name().toLowerCase();
        try (final PreparedStatement statement = this.connection.prepareStatement(
                "SELECT " + settingColumn + " FROM " + settingsTable() + " WHERE unique_id = ?"
        )) {
            statement.setString(1, uniqueId.toString());

            try (final ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    if (this.databaseType == DatabaseType.POSTGRESQL) return resultSet.getBoolean(settingColumn);

                    return resultSet.getInt(settingColumn) != 1;
                }
            }
            return true;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toggleSetting(final @NotNull UUID uniqueId, final @NotNull DatabaseSettingsType type, final boolean value) {
        if (this.connection == null) return;

        final String settingColumn = type.name().toLowerCase();
        try (final PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO " + this.settingsTable() + " (unique_id, " + settingColumn + ") VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE " + settingColumn + " = ?"
        )) {
            statement.setString(1, uniqueId.toString());
            if (this.databaseType == DatabaseType.POSTGRESQL) {
                statement.setBoolean(2, value);
                statement.setBoolean(3, value);
            } else if (this.databaseType == DatabaseType.MYSQL || this.databaseType == DatabaseType.MARIADB) {
                statement.setInt(2, value ? 0 : 1);
                statement.setInt(3, value ? 0 : 1);
            }

            statement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (this.dataSource != null) this.dataSource.close();
        if (this.connection != null) this.connection.close();
    }

    private @NotNull String settingsTable() {
        return this.sqlConfig.tablePrefix() + "settings";
    }
}
