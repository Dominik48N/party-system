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

package com.github.dominik48n.party.config;

import com.google.common.collect.Maps;
import java.text.MessageFormat;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class MessageConfig {

    static @NotNull MessageConfig fromDocument(final @NotNull Document document) {
        final MessageConfig config = new MessageConfig();

        loadMessages(config, document, "");
        config.prefix = config.messages.getOrDefault("prefix", new MessageFormat(config.prefix)).format(null);
        return config;
    }

    private static void loadMessages(final @NotNull MessageConfig config, final @NotNull Document document, final @NotNull String keyPrefix) {
        for (final String key : document.keys()) {
            if (document.isDocument(key)) {
                loadMessages(config, document.getDocument(key), key);
                continue;
            }

            try {
                final MessageFormat messageFormat = new MessageFormat(document.getString(key, "weird message"));
                config.messages.put((keyPrefix.isEmpty() ? "" : keyPrefix + ".") + key, messageFormat);
            } catch (final IllegalArgumentException ignored) {
            }
        }
    }

    private final @NotNull Map<String, MessageFormat> messages = Maps.newHashMap();
    private @NotNull String prefix = "<gray>[<gradient:#d896ff:#be29ec>Party</gradient>]";

    public @NotNull Component getMessage(final @NotNull String key, final @NotNull Object... replacements) {
        final MessageFormat messageFormat = this.messages.getOrDefault(key, new MessageFormat(key));
        final String messageString = messageFormat.format(replacements);
        return MiniMessage.miniMessage().deserialize(messageString.replace("%prefix%", this.prefix));
    }

    public @NotNull Document toDocument() {
        return new Document().append("prefix", this.prefix);
    }
}
