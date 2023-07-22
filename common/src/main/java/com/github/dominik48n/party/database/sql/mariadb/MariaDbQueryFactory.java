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

import com.github.dominik48n.party.database.sql.mysql.MySqlQueryFactory;
import de.chojo.sadu.databases.MariaDb;
import de.chojo.sadu.updater.QueryReplacement;
import de.chojo.sadu.updater.SqlUpdater;
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MariaDbQueryFactory extends MySqlQueryFactory {

    MariaDbQueryFactory(final @Nullable DataSource dataSource, final @NotNull String tablePrefix) {
        super(dataSource, tablePrefix);
    }

    @Override
    public void executeUpdater() throws IOException, SQLException {
        SqlUpdater.builder(super.source(), MariaDb.get())
                .setReplacements(new QueryReplacement("table_prefix.", super.tablePrefix))
                .setVersionTable(super.tablePrefix + "version")
                .execute();
    }
}
