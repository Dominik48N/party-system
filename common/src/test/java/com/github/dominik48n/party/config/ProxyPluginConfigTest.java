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
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProxyPluginConfigTest {

    private File testFile;
    private ProxyPluginConfig proxyPluginConfig;

    @BeforeEach
    void setUp() {
        this.testFile = new File("testFile.json");
        this.proxyPluginConfig = new ProxyPluginConfig();
    }

    @AfterEach
    void deleteFile() {
        if (this.testFile != null) this.testFile.deleteOnExit();
    }

    @Test
    void writeToFileAndReadFromFileTest() throws IOException {
        this.proxyPluginConfig.writeToFile(this.testFile);
        assertTrue(this.testFile.exists());

        final ProxyPluginConfig readConfig = ProxyPluginConfig.fromFile(this.testFile);

        assertNotNull(readConfig.redisConfig());
        assertNotNull(readConfig.messageConfig());
        assertNotNull(readConfig.databaseConfig());
        assertNotNull(readConfig.partyConfig());
        assertTrue(readConfig.updateChecker());
    }

    @Test
    void fromNonExistentFileTest() {
        assertThrows(FileNotFoundException.class, () -> ProxyPluginConfig.fromFile(new File("non-existent-file.json")));
    }
}
