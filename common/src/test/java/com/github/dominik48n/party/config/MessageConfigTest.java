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

import java.text.MessageFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageConfigTest {

    @Test
    public void testFromDocument() {
        final Document document = mock(Document.class);
        when(document.getString("prefix", "<gray>[<gradient:#d896ff:#be29ec>Party</gradient>]"))
                .thenReturn("<gray>[<gradient:#d896ff:#be29ec>PartySystem</gradient>]");
        when(document.getDocument("general")).thenReturn(mock(Document.class));
        when(document.getDocument("party")).thenReturn(mock(Document.class));
        when(document.getDocument("command")).thenReturn(mock(Document.class));

        final MessageConfig config = MessageConfig.fromDocument(document);

        assertNotNull(config);
        assertEquals("<gray>[<gradient:#d896ff:#be29ec>PartySystem</gradient>]", config.prefix());
    }

    @Test
    public void testGetMessage() {
        final MessageConfig config = new MessageConfig();
        config.messages().put("test", new MessageFormat("<dark_red>{0}</dark_red>"));
        final String expected = "<dark_red>Hello</dark_red>";

        final Component message = config.getMessage("test", "Hello");

        assertNotNull(message);
        assertEquals(expected, MiniMessage.miniMessage().serialize(message));
    }
}
