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
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Protocol;

public record RedisConfig(@NotNull List<HostAndPort> hosts, @NotNull String username, @NotNull String password) {

    static @NotNull RedisConfig fromDocument(final @NotNull Document document) throws IOException {
        final List<HostAndPort> hosts = new ArrayList<>();
        for (final String addresses : document.getStringList("hosts")) {
            final String[] split = addresses.split(":");
            final int port = split.length > 1 ? Integer.parseInt(split[1]) : Protocol.DEFAULT_PORT;
            hosts.add(new HostAndPort(split[0], port));
        }

        return new RedisConfig(
                hosts,
                document.getString("username", ""),
                document.getString("password", "secret")
        );
    }

    static @NotNull List<String> hostsToStringList(final @NotNull List<HostAndPort> hosts) {
        return hosts.stream().map(hostAndPort -> hostAndPort.getHost() + ":" + hostAndPort.getPort()).toList();
    }

    @NotNull Document toDocument() {
        return new Document()
                .append("hosts", hostsToStringList(this.hosts))
                .append("username", this.username)
                .append("password", this.password);
    }
}
