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
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkUser<TUser> implements PartyPlayer {

    private final @NotNull UserManager<TUser> userManager;
    private final @NotNull UUID uniqueId;
    private final @NotNull String name;
    private final int memberLimit;

    private @Nullable UUID partyId;

    public NetworkUser(final @NotNull PartyPlayer player, final @NotNull UserManager<TUser> userManager) {
        this.userManager = userManager;
        this.uniqueId = player.uniqueId();
        this.name = player.name();
        this.partyId = player.partyId().orElse(null);
        this.memberLimit = player.memberLimit();
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
    public int memberLimit() {
        return this.memberLimit;
    }

    @Override
    public @NotNull Optional<UUID> partyId() {
        return Optional.ofNullable(this.partyId);
    }

    @Override
    public void partyId(final @Nullable UUID partyId) {
        this.partyId = partyId;
    }

    @Override
    public void sendMessage(final @NotNull String messageKey, final @NotNull Object... replacements) {
        this.userManager.sendMessage(this.uniqueId, this.userManager.messageConfig().getMessage(messageKey, replacements));
    }
}
