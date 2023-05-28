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

package com.github.dominik48n.party.user;

import com.github.dominik48n.party.api.player.PartyPlayer;
import net.kyori.adventure.text.Component;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import java.util.UUID;

public class UserManagerTest {

    private UserManager<UserMock> userManager;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        this.userManager = Mockito.mock(UserManager.class);
    }

    @Test
    public void testGetPlayer() {
        final UserMock userMock = new UserMock(UUID.randomUUID(), "Dominik48N", this.userManager);

        final PartyPlayer expectedPlayer = Mockito.mock(PartyPlayer.class);
        Mockito.when(this.userManager.getPlayer(userMock)).thenReturn(Optional.of(expectedPlayer));

        final Optional<PartyPlayer> actualPlayer = this.userManager.getPlayer(userMock);
        assertTrue(actualPlayer.isPresent());
        assertEquals(expectedPlayer, actualPlayer.get());
    }

    @Test
    public void testUserFromCache() {
        final UUID playerId = UUID.randomUUID();

        final PartyPlayer expectedPlayer = Mockito.mock(PartyPlayer.class);
        Mockito.when(this.userManager.userFromCache(playerId)).thenReturn(Optional.of(expectedPlayer));

        final Optional<PartyPlayer> actualPlayer = this.userManager.userFromCache(playerId);
        assertTrue(actualPlayer.isPresent());
        assertEquals(expectedPlayer, actualPlayer.get());
    }

    @Test
    public void testRemovePlayerFromCache() {
        final UserMock userMock = new UserMock(UUID.randomUUID(), "Dominik48N", this.userManager);

        this.userManager.removePlayerFromCache(userMock);
        Mockito.verify(this.userManager).removePlayerFromCache(userMock);
    }

    @Test
    public void testSendMessage() {
        final UUID uniqueId = UUID.randomUUID();
        final Component component = Component.text("Test Message");

        this.userManager.sendMessage(uniqueId, component);
        Mockito.verify(this.userManager).sendMessage(uniqueId, component);
    }
}

