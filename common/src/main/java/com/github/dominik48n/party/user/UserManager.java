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

package com.github.dominik48n.party.user;

import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.MessageConfig;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class UserManager<TUser> {

    public @NotNull PartyPlayer createPlayer(final @NotNull TUser user) {
        return new User<>(user, this);
    }

    protected abstract void sendMessage(final @NotNull TUser user, final @NotNull Component component);

    protected abstract @NotNull String playerName(final @NotNull TUser user);

    protected abstract @NotNull UUID playerUUID(final @NotNull TUser user);

    protected abstract @NotNull MessageConfig messageConfig();
}