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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.util.ReflectionUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(value.getString("innerKey", ""), this.document.getDocument(key).getString("innerKey", ""));
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

    @Test
    void testAppendAndGetList() {
        final List<String> list = of("Value1", "Value2", "Value3");

        this.document.append("key", list, Document::addJsonConsumer);

        List<String> retrievedList = this.document.getList("key", new ArrayList<>(), Document.stringCollector());

        assertEquals(list, retrievedList);
    }

    @Test
    void testAddJsonConsumer() {
        final ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.withExactBigDecimals(false));
        Document.addJsonConsumer("Value", arrayNode);

        assertEquals("Value", arrayNode.get(0).asText());
    }

    @Test
    void testStringCollector() {
        final Collector<JsonNode, List<String>, List<String>> collector = Document.stringCollector();

        final List<String> stringList1 = new ArrayList<>();
        stringList1.add("Value1");

        final List<String> stringList2 = new ArrayList<>();
        stringList2.add("Value2");

        final List<String> combinedList = collector.combiner().apply(stringList1, stringList2);

        assertTrue(combinedList.containsAll(stringList1));
        assertTrue(combinedList.containsAll(stringList2));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testStringListCombiner() throws InvocationTargetException, IllegalAccessException {
        final List<String> stringList1 = new ArrayList<>();
        stringList1.add("Value1");

        final List<String> stringList2 = new ArrayList<>();
        stringList2.add("Value2");

        final Optional<Method> stringListCombiner = ReflectionUtils.findMethod(Document.class,
                "stringListCombiner",
                List.class, List.class
        );

        assertTrue(stringListCombiner.isPresent());

        final Method method = stringListCombiner.get();
        method.setAccessible(true);
        final List<String> result = (List<String>) method.invoke(null, stringList1, stringList2);
        method.setAccessible(false);

        assertTrue(result.containsAll(stringList1));
        assertTrue(result.containsAll(stringList2));
    }
}

