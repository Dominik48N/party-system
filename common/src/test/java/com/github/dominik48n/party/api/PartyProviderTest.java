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
import com.github.dominik48n.party.config.MessageConfig;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.user.UserManager;
import com.github.dominik48n.party.user.UserMock;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

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
}
