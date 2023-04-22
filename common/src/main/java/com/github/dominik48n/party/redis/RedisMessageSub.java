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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.user.UserManager;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class RedisMessageSub<TUser> extends RedisSubscription {

    public static final @NotNull String CHANNEL = "party:message";

    private final @NotNull UserManager<TUser> userManager;

    RedisMessageSub(final @NotNull UserManager<TUser> userManager) {
        this.userManager = userManager;
    }

    @Override
    public void onMessage(final @NotNull String message) {
        final Document document;
        try {
            document = new Document((ObjectNode) Document.MAPPER.readTree(message));
        } catch (final JsonProcessingException ignored) {
            return;
        }

        final UUID uniqueId;
        try {
            uniqueId = UUID.fromString(document.getString("unique_id", UUID.randomUUID().toString()));
        } catch (final IllegalArgumentException ignored) {
            return;
        }

        this.userManager.sendMessageToLocalUser(
                uniqueId,
                MiniMessage.miniMessage().deserialize(document.getString("message", "<red>Unknown Party Message :("))
        );
    }

    @Override
    public @NotNull String channel() {
        return CHANNEL;
    }
}
