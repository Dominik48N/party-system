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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static com.github.dominik48n.party.config.Document.stringCollector;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class SwitchServerConfigTest {

    private final List<String> whiteRegex = of("Lobby1", "Lobby2");
    private final List<String> blackRegex = of("PrivateServer");

    private SwitchServerConfig config;

    @BeforeEach
    void setUp() {
        this.config = createConfig(whiteRegex, blackRegex);
    }

    @Test
    public void testToDocument() {
        // Arrange

        // Act
        Document result = config.toDocument();

        // Assert
        Document serverWhiteListDocument = result.getDocument("server_whitelist");
        assertTrue(serverWhiteListDocument.getBoolean("enable", false));
        assertEquals(whiteRegex, serverWhiteListDocument.getList("regex", emptyList(), stringCollector()));
        Document serverBlackListDocument = result.getDocument("server_blacklist");
        assertFalse(serverBlackListDocument.getBoolean("enable", true));
        assertEquals(blackRegex, serverBlackListDocument.getList("regex", emptyList(), stringCollector()));
    }

    @Test
    public void testFromDocument() {
        // Arrange

        // Act

        // Assert
        assertTrue(config.whiteEnable());
        assertLinesMatch(Stream.of("Lobby1", "Lobby2"), config.whitePatternList().stream().map(Pattern::toString));
        assertFalse(config.blackEnable());
        assertLinesMatch(Stream.of("PrivateServer"), config.blackPatternList().stream().map(Pattern::toString));
    }

    private SwitchServerConfig createConfig(List<String> whiteRegex, List<String> blackRegex) {
        boolean whiteEnable = true;
        boolean blackEnable = false;

        Document document = new Document()
                .append("server_whitelist", createDocumentWith(whiteEnable, whiteRegex))
                .append("server_blacklist", createDocumentWith(blackEnable, blackRegex));

        return SwitchServerConfig.fromDocument(document);
    }

    private Document createDocumentWith(boolean enable, List<String> stringList)  {
        return new Document()
                .append("enable", enable)
                .append("regex", stringList, Document::addJsonConsumer);
    }

}