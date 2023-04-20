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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentTest {

    private Document document;

    @BeforeEach
    void setUp() {
        this.document = new Document();
    }

    @Test
    void testAppendAndGet() {
        final String key = "key";
        final String value = "value";

        this.document.append(key, value);
        assertEquals(value, this.document.getString(key, ""));
    }

    @Test
    void testAppendIntAndGet() {
        final String key = "key";
        final int value = 123;

        this.document.append(key, value);
        assertEquals(value, this.document.getInt(key, 0));
    }

    @Test
    void testAppendDocumentAndGet() {
        final String key = "key";
        final Document value = new Document();

        value.append("innerKey", "innerValue");
        this.document.append(key, value);
        assertEquals(value, this.document.getDocument(key));
    }

    @Test
    void testWriteAndRead(@TempDir final File tempDir) throws IOException {
        final String key = "key";
        final String value = "value";
        final File file = new File(tempDir, "test.json");

        this.document.append(key, value);
        this.document.writeToFile(file);

        final Document readDocument = Document.read(file);
        assertEquals(value, readDocument.getString(key, ""));
    }

    @Test
    void testReadNonExistentFile() {
        final File file = new File("non-existent.json");
        assertThrows(FileNotFoundException.class, () -> Document.read(file));
    }
}

