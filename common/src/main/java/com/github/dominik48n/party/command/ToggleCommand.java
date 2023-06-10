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

package com.github.dominik48n.party.command;

import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.database.DatabaseAdapter;
import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ToggleCommand extends PartyCommand {

    private final @NotNull DatabaseAdapter databaseAdapter;

    public ToggleCommand(final @NotNull DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
    }

    @Override
    public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
        if (args.length != 1) {
            player.sendMessage("command.usage.toggle");
            return;
        }

        final Optional<DatabaseSettingsType> type = Arrays.stream(DatabaseSettingsType.values())
                .filter(settingsType -> settingsType.name().equalsIgnoreCase(args[0]))
                .findAny();
        if (type.isEmpty()) {
            player.sendMessage("command.usage.toggle");
            return;
        }

        final boolean value = this.databaseAdapter.getSettingValue(player.uniqueId(), type.get());
        this.databaseAdapter.toggleSetting(player.uniqueId(), type.get(), !value);
        player.sendMessage("command.toggle." + type.get().name().toLowerCase() + "." + (value ? "disabled" : "enabled"));
    }

    @Override
    @NotNull List<String> tabComplete(final @NotNull String[] args) {
        if (args.length != 1) return super.tabComplete(args);

        final String search = args[0].toLowerCase();
        return Arrays.stream(DatabaseSettingsType.values()).map(type -> type.name().toLowerCase()).filter(s -> s.startsWith(search)).toList();
    }
}
