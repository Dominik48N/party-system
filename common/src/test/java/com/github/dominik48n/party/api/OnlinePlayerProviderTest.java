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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
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

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.onlinePlayerProvider = new DefaultOnlinePlayersProvider<>(this.redisManager, this.userManager);
        when(this.redisManager.jedisPool()).thenReturn(this.jedisPool);
        when(this.jedisPool.getResource()).thenReturn(this.jedis);
    }

    @Test
    public void testGetByUsername() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);

        final Set<String> keys = new HashSet<>();
        keys.add(this.playerKey);
        when(this.jedis.keys("party_player:*")).thenReturn(keys);
        when(this.jedis.get(this.playerKey)).thenReturn(Document.MAPPER.writeValueAsString(partyPlayer));

        final Optional<PartyPlayer> result = this.onlinePlayerProvider.get(username);
        assertTrue(result.isPresent());
        assertEquals(partyPlayer, result.get());
    }

    @Test
    public void testGetByUniqueId() throws JsonProcessingException {
        final PartyPlayer partyPlayer = new UserMock(this.uniqueId, this.username, this.userManager);

        when(this.jedis.get(this.playerKey)).thenReturn(Document.MAPPER.writeValueAsString(partyPlayer));

        final Optional<PartyPlayer> result = this.onlinePlayerProvider.get(this.uniqueId);
        assertTrue(result.isPresent());
        assertEquals(partyPlayer, result.get());
    }
}
