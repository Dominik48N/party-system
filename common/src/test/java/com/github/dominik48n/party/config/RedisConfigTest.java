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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.HostAndPort;

public class RedisConfigTest {

    @Test
    void testFromDocument() throws IOException {
        final List<HostAndPort> hosts = Collections.singletonList(new HostAndPort("redis.example.com", 3133));
        final String user = "admin", password = "topsecret";

        final Document document = new Document()
                .append("hosts", RedisConfig.hostsToStringList(hosts))
                .append("username", user)
                .append("password", password);
        final RedisConfig config = RedisConfig.fromDocument(document);

        assertEquals(hosts, config.hosts());
        assertEquals(user, config.username());
        assertEquals(password, config.password());
    }

    @Test
    void testToDocument() throws IOException {
        final List<HostAndPort> hosts = Collections.singletonList(new HostAndPort("redis.example.com", 5555));
        final String user = "admin", password = "topsecret";

        final RedisConfig config = new RedisConfig(hosts, user, password);
        final Document document = config.toDocument();

        assertEquals(RedisConfig.hostsToStringList(hosts), document.getStringList("hosts"));
        assertEquals(user, document.getString("username", "incorrect username"));
        assertEquals(password, document.getString("password", "incorrect password"));
    }
}
