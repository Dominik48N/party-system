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

package com.github.dominik48n.party.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import com.github.dominik48n.party.user.UserMock;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.UnifiedJedis;

public class PartyProviderTest {

    @Mock
    private RedisManager redisManager;

    @Mock
    private UserManager<UserMock> userManager;

    @Mock
    private MessageConfig messageConfig;

    private PartyProvider partyProvider;

    @Mock
    private UnifiedJedis jedis;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        this.mocks = MockitoAnnotations.openMocks(this);
        this.partyProvider = new DefaultPartyProvider<>(this.redisManager, this.userManager, this.messageConfig);
        when(this.redisManager.jedis()).thenReturn(this.jedis);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.mocks.close();
    }

    @Test
    void testCreateParty() throws JsonProcessingException {
        final UUID leader = UUID.randomUUID();
        final int maxMembers = 12;

        final Party party = this.partyProvider.createParty(leader, maxMembers);

        assertNotNull(party);
        assertEquals(leader, party.leader());
        assertEquals(maxMembers, party.maxMembers());
    }

    @Test
    void testPartyDelete() {
        final UUID partyId = UUID.randomUUID();

        when(this.redisManager.jedis()).thenReturn(this.jedis);

        this.partyProvider.deleteParty(partyId);

        verify(jedis).del("party:" + partyId);
    }

    @Test
    void testCreateAndDeletePartyRequest() {
        final String source = "Dominik48N";
        final String target = "randomUser";
        final int expires = 25;

        when(this.redisManager.jedis()).thenReturn(this.jedis);

        // Request create
        this.partyProvider.createPartyRequest(source, target, expires);
        verify(this.jedis).setex("request:" + source + ":" + target, expires, "");

        // Request delete
        this.partyProvider.removePartyRequest(source, target);
        verify(this.jedis).del("request:" + source + ":" + target);
    }

    @Test
    void testExistsPartyRequest() {
        final String source = "Dominik48N";
        final String target = "randomUser";

        when(this.jedis.exists("request:" + source + ":" + target)).thenReturn(true);
        assertTrue(this.partyProvider.existsPartyRequest(source, target));

        when(this.jedis.exists("request:" + source + ":" + target)).thenReturn(false);
        assertFalse(this.partyProvider.existsPartyRequest(source, target));
    }
}
