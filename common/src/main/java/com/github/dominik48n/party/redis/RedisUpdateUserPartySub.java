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

package com.github.dominik48n.party.redis;

import com.github.dominik48n.party.user.UserManager;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class RedisUpdateUserPartySub<TUser> extends RedisSubscription {

    public static final @NotNull String CHANNEL = "party:update_user_party";

    private final @NotNull UserManager<TUser> userManager;

    public RedisUpdateUserPartySub(final @NotNull UserManager<TUser> userManager) {
        this.userManager = userManager;
    }

    @Override
    public void onMessage(final @NotNull String message) {
        final String[] split = message.split(":");
        if (split.length != 2) return;

        final UUID uniqueId;
        try {
            uniqueId = UUID.fromString(split[0]);
        } catch (final IllegalArgumentException ignored) {
            return;
        }

        this.userManager.userFromCache(uniqueId).ifPresent(player -> {
            final UUID partyId;
            try {
                final String s = split[1];
                if (s.equals("null")) partyId = null;
                else partyId = UUID.fromString(s);
            } catch (final IllegalArgumentException ignored) {
                return;
            }

            player.partyId(partyId);
        });
    }

    @Override
    public @NotNull String channel() {
        return CHANNEL;
    }
}
