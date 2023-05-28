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
import com.github.dominik48n.party.api.player.OnlinePlayerProvider;
import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.redis.RedisMessageSub;
import com.github.dominik48n.party.user.UserManager;
import com.github.dominik48n.party.user.UserMock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class PartyProviderTest {

    @Mock
    private RedisManager redisManager;

    @Mock
    private UserManager<UserMock> userManager;

    @Mock
    private MessageConfig messageConfig;

    @Mock
    private OnlinePlayerProvider onlinePlayerProvider;

    private PartyProvider partyProvider;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.partyProvider = new DefaultPartyProvider<>(this.redisManager, this.userManager, this.messageConfig);
    }

    @Test
    public void testGetPartyFromPlayer() throws Exception {
        final UUID playerId = UUID.randomUUID();
        final UUID partyId = UUID.randomUUID();

        final UserMock userMock = new UserMock(playerId, "Dominik48N", this.userManager);
        when(this.onlinePlayerProvider.get(playerId)).thenReturn(Optional.of(userMock));
        when(this.userManager.getPlayer(userMock)).thenReturn(Optional.of(userMock));
        when(userMock.partyId()).thenReturn(Optional.of(partyId));

        final Optional<UUID> result = this.partyProvider.getPartyFromPlayer(partyId);
        assertEquals(partyId, result.orElse(null));
    }

    @Test
    public void testCreateParty() throws JsonProcessingException {
        final UUID leader = UUID.randomUUID();
        final int maxMembers = 12;

        final Party party = this.partyProvider.createParty(leader, maxMembers);

        assertNotNull(party);
        assertEquals(leader, party.leader());
        assertEquals(maxMembers, party.maxMembers());
    }

    @Test
    public void testAddPlayerToParty() throws JsonProcessingException {
        final UUID partyId = UUID.randomUUID();
        final UUID player = UUID.randomUUID();
        final Party party = new Party(partyId, UUID.randomUUID(), new ArrayList<>(), 12);

        when(this.redisManager.jedisPool().getResource()).thenReturn(mock(Jedis.class));
        when(this.redisManager.jedisPool().getResource().get("party:" + party)).thenReturn(Document.MAPPER.writeValueAsString(party));

        this.partyProvider.addPlayerToParty(partyId, player);

        assertTrue(party.members().contains(player));
    }

    @Test
    public void testRemovePlayerFromParty() throws JsonProcessingException {
        final UUID partyId = UUID.randomUUID();
        final UUID player = UUID.randomUUID();
        final Party party = new Party(partyId, UUID.randomUUID(), new ArrayList<>(), 2);

        when(this.redisManager.jedisPool().getResource()).thenReturn(mock(Jedis.class));
        when(this.redisManager.jedisPool().getResource().get("party:" + partyId)).thenReturn(Document.MAPPER.writeValueAsString(party));

        this.partyProvider.removePlayerFromParty(partyId, player, "Dominik48N");

        assertFalse(party.members().contains(player));
    }

    @Test
    public void testPartyLeaderChange() throws JsonProcessingException {
        final UUID partyId = UUID.randomUUID();
        final UUID oldLeader = UUID.randomUUID();
        final UUID newLeader = UUID.randomUUID();
        final Party party = new Party(partyId, oldLeader, List.of(oldLeader), 64);

        when(this.redisManager.jedisPool().getResource()).thenReturn(mock(Jedis.class));
        when(this.redisManager.jedisPool().getResource().get("party:" + partyId)).thenReturn(Document.MAPPER.writeValueAsString(party));

        this.partyProvider.changePartyLeader(partyId, oldLeader, newLeader, 32);

        assertEquals(newLeader, party.leader());
        assertFalse(party.members().contains(newLeader));
        assertTrue(party.members().contains(oldLeader));
    }

    @Test
    public void testSendMessageToParty() {
        final Party party = mock(Party.class);
        final List<UUID> members = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(party.allMembers()).thenReturn(members);

        final String messageKey = "test.party.message";
        final Object[] replacements = {"github.com/Dominik48N", 3500L, 5.3D, 10};

        this.partyProvider.sendMessageToParty(party, messageKey, replacements);
        verify(this.redisManager, times(members.size())).publish(eq(RedisMessageSub.CHANNEL), any(Document.class));
    }

    @Test
    public void testPartyDelete() {
        final UUID partyId = UUID.randomUUID();
        final Jedis jedis = mock(Jedis.class);
        final JedisPool jedisPool = mock(JedisPool.class);

        when(this.redisManager.jedisPool()).thenReturn(jedisPool);
        when(jedisPool.getResource()).thenReturn(jedis);

        this.partyProvider.deleteParty(partyId);

        verify(jedis).del("party:" + partyId);
    }
}
