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

package com.github.dominik48n.party.player;

import com.github.dominik48n.party.api.player.PartyPlayer;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class Player<TPlayer> implements PartyPlayer {

    private final @NotNull PlayerManager<TPlayer> playerManager;
    private final @NotNull TPlayer player;
    private final @NotNull UUID uniqueId;
    private final @NotNull String name;

    Player(
            final @NotNull TPlayer player,
            final @NotNull PlayerManager<TPlayer> playerManager
    ) {
        this.player = player;
        this.playerManager = playerManager;
        this.uniqueId = playerManager.playerUUID(player);
        this.name = playerManager.playerName(player);
    }

    @Override
    public @NotNull UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public void sendMessage(final @NotNull String messageKey, final @NotNull Object... replacements) {
        this.playerManager.sendMessage(this.player, Component.text(messageKey)); // TODO
    }
}
