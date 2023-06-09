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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.dominik48n.party.api.player.PartyPlayer;
import java.io.IOException;
import java.util.UUID;

public class UserSerializer extends StdSerializer<PartyPlayer> {

    public UserSerializer(final Class<PartyPlayer> t) {
        super(t);
    }

    @Override
    public void serialize(final PartyPlayer value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("uuid", value.uniqueId().toString());
        gen.writeStringField("name", value.name());
        gen.writeStringField("party_id", value.partyId().map(UUID::toString).orElse(null));
        gen.writeNumberField("member_limit", value.memberLimit());
        gen.writeEndObject();
    }
}
