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

package com.github.dominik48n.party.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.PartyProvider;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.SwitchServerConfig;
import com.github.dominik48n.party.user.UserManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import static java.util.List.of;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SwitchServerTest {

    private SwitchServer<String> switchServer;

    @Mock
    private UserManager<String> userManager;

    @Mock
    private SwitchServerConfig switchServerConfig;

    @Mock
    private PartyPlayer partyPlayer;

    @Mock
    private PartyProvider partyProvider;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        setPartyProvider();
        switchServer = createSwitchServer();
    }

    @Test
    void handleServerConnected() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(this.userManager.getPlayer(user)).thenReturn(Optional.of(this.partyPlayer));
        when(this.partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(this.partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(this.partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(this.switchServerConfig.blackEnable()).thenReturn(true);
        when(this.switchServerConfig.blackPatternList()).thenReturn(of(Pattern.compile("^Lobby.*")));
        when(this.switchServerConfig.whiteEnable()).thenReturn(true);
        when(this.switchServerConfig.whitePatternList()).thenReturn(of(Pattern.compile("Lobby3")));

        this.switchServer.handleServerConnected(user, serverName);

        verify(this.partyProvider).connectPartyToServer(party, serverName);
    }

    @Test
    void handleServerConnectedFailBecauseNotMatchWhiteList() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby2";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(this.userManager.getPlayer(user)).thenReturn(Optional.of(this.partyPlayer));
        when(this.partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(this.partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(this.partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(this.switchServerConfig.blackEnable()).thenReturn(true);
        when(this.switchServerConfig.blackPatternList()).thenReturn(of(Pattern.compile("^Lobby.*")));
        when(this.switchServerConfig.whiteEnable()).thenReturn(true);
        when(this.switchServerConfig.whitePatternList()).thenReturn(of(Pattern.compile("Lobby3")));

        this.switchServer.handleServerConnected(user, serverName);

        verify(this.partyProvider, never()).connectPartyToServer(any(), any());
    }

    @Test
    void handleServerConnectedDisableWhiteList() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(this.userManager.getPlayer(user)).thenReturn(Optional.of(this.partyPlayer));
        when(this.partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(this.partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(this.partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(this.switchServerConfig.blackEnable()).thenReturn(true);
        when(this.switchServerConfig.blackPatternList()).thenReturn(of(Pattern.compile("^Lobby.*")));
        when(this.switchServerConfig.whiteEnable()).thenReturn(false);

        this.switchServer.handleServerConnected(user, serverName);

        verify(this.partyProvider, never()).connectPartyToServer(any(), any());
        verify(this.switchServerConfig, never()).whitePatternList();
    }

    @Test
    void handleServerConnectedDisableBlackList() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(this.userManager.getPlayer(user)).thenReturn(Optional.of(this.partyPlayer));
        when(this.partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(this.partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(this.partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(this.switchServerConfig.blackEnable()).thenReturn(false);

        this.switchServer.handleServerConnected(user, serverName);

        verify(this.partyProvider).connectPartyToServer(party, serverName);
        verify(this.switchServerConfig, never()).blackPatternList();
        verify(this.switchServerConfig, never()).whiteEnable();
        verify(this.switchServerConfig, never()).whitePatternList();
    }


    @Test
    void handleServerConnectedNotPartyLeader() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(this.userManager.getPlayer(user)).thenReturn(Optional.of(this.partyPlayer));
        when(this.partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(this.partyPlayer.uniqueId()).thenReturn(UUID.randomUUID());
        when(this.partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        this.switchServer.handleServerConnected(user, serverName);

        verify(this.partyProvider, never()).connectPartyToServer(party, serverName);
        verify(this.switchServerConfig, never()).blackEnable();
    }

    @Test
    void expectJsonProcessingException() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(this.userManager.getPlayer(user)).thenReturn(Optional.of(this.partyPlayer));
        when(this.partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(this.partyProvider.getParty(partyUuid)).thenThrow(JsonProcessingException.class);

        this.switchServer.handleServerConnected(user, serverName);

        verify(this.partyProvider, never()).connectPartyToServer(party, serverName);
    }

    private void setPartyProvider() throws IllegalAccessException {
        Field partyProviderField = ReflectionUtils.findFields(PartyAPI.class, field -> field.getType().equals(PartyProvider.class),
                TOP_DOWN).get(0);
        partyProviderField.setAccessible(true);
        partyProviderField.set(null, this.partyProvider);
        partyProviderField.setAccessible(false);
    }

    private @NotNull SwitchServer<String> createSwitchServer() {
        return new SwitchServer<>(this.userManager, this.switchServerConfig) {
            @Override
            public void logJsonProcessingException(final @NotNull JsonProcessingException jsonProcessingException) { }
        };
    }

    // Mockito can not Mock final classes.
    private @NotNull Party createParty(final @NotNull UUID partyId, final @NotNull UUID leader) {
        return new Party(partyId, leader, of(UUID.randomUUID()), 2);
    }
}