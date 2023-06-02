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
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.Document;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import com.github.dominik48n.party.user.UserMock;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class OnlinePlayerProviderTest {

    private final @NotNull String username = "Dominik48N";
    private final @NotNull UUID uniqueId = UUID.randomUUID();
    private final @NotNull String playerKey = "party_player:" + this.uniqueId;

    private OnlinePlayerProvider onlinePlayerProvider;

    @Mock
    private RedisManager redisManager;

    @Mock
    private UserManager<UserMock> userManager;

    @Mock
    private JedisPool jedisPool;

    @Mock
    private Jedis jedis;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        this.mocks = MockitoAnnotations.openMocks(this);
        this.onlinePlayerProvider = new DefaultOnlinePlayersProvider<>(this.redisManager, this.userManager);
        when(this.redisManager.jedisPool()).thenReturn(this.jedisPool);
        when(this.jedisPool.getResource()).thenReturn(this.jedis);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.mocks.close();
    }

    @Test
    void testGetByUsername() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);

        final Set<String> keys = new HashSet<>();
        keys.add(this.playerKey);
        when(this.jedis.keys("party_player:*")).thenReturn(keys);
        final String jsonString = Document.MAPPER.writeValueAsString(partyPlayer);
        when(this.jedis.get(this.playerKey)).thenReturn(jsonString);

        final Optional<PartyPlayer> result = this.onlinePlayerProvider.get(username);
        assertTrue(result.isPresent());
        assertEquals(partyPlayer, result.get());
    }

    @Test
    void testGetByUniqueId() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);
        final String jsonString = Document.MAPPER.writeValueAsString(partyPlayer);
        when(this.jedis.get(this.playerKey)).thenReturn(jsonString);

        final Optional<PartyPlayer> result = this.onlinePlayerProvider.get(this.uniqueId);
        assertTrue(result.isPresent());
        assertEquals(partyPlayer, result.get());
    }

    @Test
    void testGetByUsernameNotFound() throws JsonProcessingException {
        when(this.jedis.keys("party_player:*")).thenReturn(new HashSet<>());

        final Optional<PartyPlayer> result = this.onlinePlayerProvider.get(this.username);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByUniqueIdNotFound() throws JsonProcessingException {
        when(this.jedis.get(this.playerKey)).thenReturn(null);

        final Optional<PartyPlayer> result = this.onlinePlayerProvider.get(this.uniqueId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByUniqueIds() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);

        final List<UUID> uniqueIds = Collections.singletonList(this.uniqueId);
        final String[] keys = {this.playerKey};
        final String[] jsonValues = {Document.MAPPER.writeValueAsString(partyPlayer)};
        when(this.jedis.mget(keys)).thenReturn(List.of(jsonValues));

        final Map<UUID, PartyPlayer> result = this.onlinePlayerProvider.get(uniqueIds);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(this.uniqueId));
        assertEquals(partyPlayer, result.get(this.uniqueId));
    }

    @Test
    void testGetByUniqueIdsNotFound() throws JsonProcessingException {
        final List<UUID> uniqueIds = Collections.singletonList(this.uniqueId);
        final String[] keys = {};
        final String[] jsonValues = {};
        when(this.jedis.mget(keys)).thenReturn(List.of(jsonValues));

        final Map<UUID, PartyPlayer> result = this.onlinePlayerProvider.get(uniqueIds);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAll() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);

        final Set<String> keys = new HashSet<>();
        keys.add(this.playerKey);
        when(this.jedis.keys("party_player:*")).thenReturn(keys);
        final String jsonString = Document.MAPPER.writeValueAsString(partyPlayer);
        when(this.jedis.get(this.playerKey)).thenReturn(jsonString);

        final List<PartyPlayer> result = this.onlinePlayerProvider.all();
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(partyPlayer::equals));
    }

    @Test
    void testGetAllNotFound() throws JsonProcessingException {
        when(this.jedis.keys("party_player:*")).thenReturn(new HashSet<>());

        final List<PartyPlayer> result = this.onlinePlayerProvider.all();
        assertTrue(result.isEmpty());
    }

    @Test
    void testLogin() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);

        this.onlinePlayerProvider.login(partyPlayer);

        verify(this.jedis).set(this.playerKey, Document.MAPPER.writeValueAsString(partyPlayer));
    }

    @Test
    void testLogoutPlayerLoggedIn() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);
        final String jsonString = Document.MAPPER.writeValueAsString(partyPlayer);
        when(this.jedis.get(eq(this.playerKey))).thenReturn(jsonString);

        this.onlinePlayerProvider.logout(this.uniqueId);

        verify(this.jedis).get(this.playerKey);
        verify(this.jedis).del(this.playerKey);
    }

    @Test
    void testLogoutPlayerNotLoggedIn() {
        when(this.jedis.get(this.playerKey)).thenReturn(null);

        this.onlinePlayerProvider.logout(this.uniqueId);

        verify(this.jedis).get(this.playerKey);
        verify(this.jedis, never()).del(this.playerKey);
    }
}
