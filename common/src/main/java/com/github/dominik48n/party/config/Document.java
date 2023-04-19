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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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

    public static final @NotNull Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static @NotNull Document read(final @NotNull File file) throws FileNotFoundException {
        if (!file.exists()) throw new FileNotFoundException("The configuration file isn't exist.");
        return new Document(GSON.fromJson(new BufferedReader(new FileReader(file)), JsonObject.class));
    }

    private final @NotNull JsonObject jsonObject;

    public Document() {
        this(new JsonObject());
    }

    public Document(final @NotNull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public @NotNull Document append(final @NotNull String key, final @NotNull String value) {
        this.jsonObject.addProperty(key, value);
        return this;
    }

    @NotNull Document append(final @NotNull String key, final @NotNull Number value) {
        this.jsonObject.addProperty(key, value);
        return this;
    }

    @NotNull Document append(final @NotNull String key, final @NotNull Document value) {
        this.jsonObject.add(key, value.jsonObject);
        return this;
    }

    public @NotNull String getString(final @NotNull String key, final @NotNull String defaultValue) {
        return this.contains(key) ? this.jsonObject.get(key).getAsString() : defaultValue;
    }

    int getInt(final @NotNull String key, final int defaultValue) {
        return this.contains(key) ? this.jsonObject.get(key).getAsInt() : defaultValue;
    }

    @NotNull Document getDocument(final @NotNull String key) {
        return this.contains(key) ? new Document(this.jsonObject.getAsJsonObject(key)) : new Document();
    }

    @NotNull Set<String> keys() {
        return this.jsonObject.keySet();
    }

    boolean isDocument(final @NotNull String key) {
        return this.contains(key) && this.jsonObject.get(key).isJsonObject();
    }

    void writeToFile(final @NotNull File file) throws IOException {
        file.deleteOnExit();

        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this.jsonObject, writer);
        }
    }

    @Override
    public String toString() {
        return GSON.toJson(this.jsonObject);
    }

    private boolean contains(final @NotNull String key) {
        return this.jsonObject.has(key);
    }
}
