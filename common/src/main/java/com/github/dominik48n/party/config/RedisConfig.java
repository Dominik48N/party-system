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

import org.jetbrains.annotations.NotNull;

public record RedisConfig(@NotNull String hostname, int port, @NotNull String username, @NotNull String password) {

    static @NotNull RedisConfig fromDocument(final @NotNull Document document) {
        return new RedisConfig(
                document.getString("hostname", "127.0.0.1"),
                document.getInt("port", 6379),
                document.getString("username", ""),
                document.getString("password", "secret")
        );
    }

    @NotNull Document toDocument() {
        return new Document()
                .append("hostname", this.hostname)
                .append("port", this.port)
                .append("username", this.username)
                .append("password", this.password);
    }
}
