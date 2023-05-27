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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class PartyConfigTest {

    @Test
    public void testFromDocument() {
        final Document document = new Document().append("request_expires", 60);
        final PartyConfig partyConfig = PartyConfig.fromDocument(document);

        assertEquals(60, partyConfig.requestExpires());
    }

    @Test
    public void testToDocument() {
        final PartyConfig partyConfig = new PartyConfig(30, false, 10);
        final Document document = partyConfig.toDocument();

        assertEquals(30, document.getInt("request_expires", -1));
        assertEquals(10, document.getInt("default_member_limit", 22));
        assertFalse(partyConfig.useMemberLimit());
    }
}
