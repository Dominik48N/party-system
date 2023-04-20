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
import org.junit.jupiter.api.Test;

public class RedisConfigTest {

    @Test
    void testFromDocument() {
        final String host = "redis.example.com", user = "admin", password = "topsecret";
        final int port = 6380;

        final Document document = new Document()
                .append("hostname", host)
                .append("port", port)
                .append("username", user)
                .append("password", password);
        final RedisConfig config = RedisConfig.fromDocument(document);

        assertEquals(host, config.hostname());
        assertEquals(port, config.port());
        assertEquals(user, config.username());
        assertEquals(password, config.password());
    }

    @Test
    void testToDocument() {
        final String host = "redis.example.com", user = "admin", password = "topsecret";
        final int port = 6380;

        final RedisConfig config = new RedisConfig(host, port, user, password);
        final Document document = config.toDocument();

        assertEquals(host, document.getString("hostname", "incorrect host"));
        assertEquals(port, document.getInt("port", -1));
        assertEquals(user, document.getString("username", "incorrect username"));
        assertEquals(password, document.getString("password", "incorrect password"));
    }
}
