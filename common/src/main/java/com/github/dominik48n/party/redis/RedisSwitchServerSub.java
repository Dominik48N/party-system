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

import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.user.UserManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class RedisSwitchServerSub<TUser> extends RedisSubscription {

    public static final @NotNull String CHANNEL = "party:server";

    private final @NotNull UserManager<TUser> userManager;

    public RedisSwitchServerSub(final @NotNull UserManager<TUser> userManager) {
        this.userManager = userManager;
    }

    @Override
    public void onMessage(final String channel, final String message) {
        if (!channel.equals(CHANNEL)) return;

        final Document document;
        try {
            document = new Document(Document.GSON.fromJson(message, JsonObject.class));
        } catch (final JsonSyntaxException ignored) {
            return;
        }

        final UUID uniqueId;
        try {
            uniqueId = UUID.fromString(document.getString("unique_id", UUID.randomUUID().toString()));
        } catch (final IllegalArgumentException ignored) {
            return;
        }

        this.userManager.connectToServer(uniqueId, document.getString("server", ""));
    }

    @Override
    public @NotNull String[] channels() {
        return new String[] {CHANNEL};
    }

    @Override
    public void close() {
        this.unsubscribe();
    }
}
