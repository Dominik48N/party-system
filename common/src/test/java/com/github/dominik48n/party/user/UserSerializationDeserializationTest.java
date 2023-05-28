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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.dominik48n.party.api.player.PartyPlayer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.UUID;

public class UserSerializationDeserializationTest {

    @Test
    public void testUserSerializationDeserialization() throws IOException {
        final PartyPlayer player = new UserDeserializer.DeserializedUser(
                UUID.randomUUID(),
                "Dominik48N",
                UUID.randomUUID(),
                8
        );

        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(PartyPlayer.class, new UserSerializer(PartyPlayer.class));
        module.addDeserializer(PartyPlayer.class, new UserDeserializer());
        objectMapper.registerModule(module);

        final String json = objectMapper.writeValueAsString(player);
        final PartyPlayer deserializedPlayer = objectMapper.readValue(json, PartyPlayer.class);

        assertEquals(player.uniqueId(), deserializedPlayer.uniqueId());
        assertEquals(player.name(), deserializedPlayer.name());
        assertEquals(player.memberLimit(), deserializedPlayer.memberLimit());
        assertEquals(player.partyId(), deserializedPlayer.partyId());
    }
}

