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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.user.UserDeserializer;
import com.github.dominik48n.party.user.UserSerializer;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class Document {

    public static final @NotNull ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setNodeFactory(JsonNodeFactory.withExactBigDecimals(true))
            .registerModule(new SimpleModule()
                    .addSerializer(PartyPlayer.class, new UserSerializer(PartyPlayer.class))
                    .addDeserializer(PartyPlayer.class, new UserDeserializer())
            );

    static @NotNull Document read(final File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException("The configuration file doesn't exist.");

        final JsonNode jsonNode = MAPPER.readTree(new BufferedReader(new FileReader(file)));
        if (!jsonNode.isObject()) throw new IllegalArgumentException("The configuration file must contain a JSON object.");
        return new Document((ObjectNode) jsonNode);
    }

    private final @NotNull ObjectNode objectNode;

    public Document() {
        this(JsonNodeFactory.instance.objectNode());
    }

    public Document(final @NotNull ObjectNode objectNode) {
        this.objectNode = objectNode;
    }

    public @NotNull Document append(final @NotNull String key, final @NotNull String value) {
        this.objectNode.put(key, value);
        return this;
    }

    @NotNull Document append(final @NotNull String key, final int value) {
        this.objectNode.put(key, value);
        return this;
    }

    @NotNull Document append(final @NotNull String key, final long value) {
        this.objectNode.put(key, value);
        return this;
    }

    @NotNull Document append(final @NotNull String key, final @NotNull Document value) {
        this.objectNode.set(key, value.objectNode);
        return this;
    }

    @NotNull Document append(final @NotNull String key, final boolean value) {
        this.objectNode.put(key, value);
        return this;
    }

    public @NotNull String getString(final @NotNull String key, final @NotNull String defaultValue) {
        return this.contains(key) ? this.objectNode.get(key).asText() : defaultValue;
    }

    int getInt(final @NotNull String key, final int defaultValue) {
        return this.contains(key) ? this.objectNode.get(key).asInt() : defaultValue;
    }

    long getLong(final @NotNull String key, final long defaultValue) {
        return this.contains(key) ? this.objectNode.get(key).asLong() : defaultValue;
    }

    boolean getBoolean(final @NotNull String key, final boolean defaultValue) {
        return this.contains(key) ? this.objectNode.get(key).asBoolean() : defaultValue;
    }

    @NotNull Document getDocument(final @NotNull String key) {
        return this.contains(key) ? new Document((ObjectNode) this.objectNode.get(key)) : new Document();
    }

    @NotNull Set<String> keys() {
        return Sets.newHashSet(this.objectNode.fieldNames());
    }

    boolean isDocument(final @NotNull String key) {
        return this.contains(key) && this.objectNode.get(key).isObject();
    }

    void writeToFile(final @NotNull File file) throws IOException {
        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            MAPPER.writeValue(writer, this.objectNode);
        }
    }

    @Override
    public @NotNull String toString() {
        try {
            return MAPPER.writeValueAsString(this.objectNode);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean contains(final @NotNull String key) {
        return this.objectNode.has(key);
    }
}
