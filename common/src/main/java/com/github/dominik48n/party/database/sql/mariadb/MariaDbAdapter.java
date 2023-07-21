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

package com.github.dominik48n.party.database.sql.mariadb;

import com.github.dominik48n.party.config.DatabaseConfig;
import com.github.dominik48n.party.database.sql.SqlAdapter;
import com.github.dominik48n.party.database.sql.SqlQueryFactory;
import de.chojo.sadu.databases.MariaDb;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.datasource.stage.ConfigurationStage;
import org.jetbrains.annotations.NotNull;

public class MariaDbAdapter extends SqlAdapter {

    public MariaDbAdapter(final DatabaseConfig.@NotNull SQLConfig sqlConfig) {
        super(sqlConfig);
    }

    @Override
    protected @NotNull SqlQueryFactory queryFactory() {
        return new MariaDbQueryFactory(super.dataSource, super.sqlConfig.tablePrefix());
    }

    @Override
    protected @NotNull ConfigurationStage configurationStage() {
        final String host = super.sqlConfig.hostname();
        final int port = super.sqlConfig.port();
        final String user = super.sqlConfig.username();
        final String password = super.sqlConfig.password();
        final String database = super.sqlConfig.database();
        return DataSourceCreator.create(MariaDb.get())
                .configure(config -> config.host(host).port(port).user(user).password(password).database(database))
                .create();
    }
}
