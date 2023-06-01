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
    public void setUp() throws IllegalAccessException {
        setPartyProvider();
        switchServer = createSwitchServer();
    }

    @Test
    public void handleServerConnected() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(userManager.getPlayer(user)).thenReturn(Optional.of(partyPlayer));
        when(partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(switchServerConfig.blackEnable()).thenReturn(true);
        when(switchServerConfig.blackPatternList()).thenReturn(of(Pattern.compile("^Lobby.*")));
        when(switchServerConfig.whiteEnable()).thenReturn(true);
        when(switchServerConfig.whitePatternList()).thenReturn(of(Pattern.compile("Lobby3")));

        switchServer.handleServerConnected(user, serverName);

        verify(partyProvider).connectPartyToServer(party, serverName);
    }

    @Test
    public void handleServerConnectedFailBecauseNotMatchWhiteList() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby2";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(userManager.getPlayer(user)).thenReturn(Optional.of(partyPlayer));
        when(partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(switchServerConfig.blackEnable()).thenReturn(true);
        when(switchServerConfig.blackPatternList()).thenReturn(of(Pattern.compile("^Lobby.*")));
        when(switchServerConfig.whiteEnable()).thenReturn(true);
        when(switchServerConfig.whitePatternList()).thenReturn(of(Pattern.compile("Lobby3")));

        switchServer.handleServerConnected(user, serverName);

        verify(partyProvider, never()).connectPartyToServer(any(), any());
    }

    @Test
    public void handleServerConnectedDisableWhiteList() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(userManager.getPlayer(user)).thenReturn(Optional.of(partyPlayer));
        when(partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(switchServerConfig.blackEnable()).thenReturn(true);
        when(switchServerConfig.blackPatternList()).thenReturn(of(Pattern.compile("^Lobby.*")));
        when(switchServerConfig.whiteEnable()).thenReturn(false);

        switchServer.handleServerConnected(user, serverName);

        verify(partyProvider, never()).connectPartyToServer(any(), any());
        verify(switchServerConfig, never()).whitePatternList();
    }

    @Test
    public void handleServerConnectedDisableBlackList() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(userManager.getPlayer(user)).thenReturn(Optional.of(partyPlayer));
        when(partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(partyPlayer.uniqueId()).thenReturn(leaderUUID);
        when(partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        when(switchServerConfig.blackEnable()).thenReturn(false);

        switchServer.handleServerConnected(user, serverName);

        verify(partyProvider).connectPartyToServer(party, serverName);
        verify(switchServerConfig, never()).blackPatternList();
        verify(switchServerConfig, never()).whiteEnable();
        verify(switchServerConfig, never()).whitePatternList();
    }


    @Test
    public void handleServerConnectedNotPartyLeader() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(userManager.getPlayer(user)).thenReturn(Optional.of(partyPlayer));
        when(partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(partyPlayer.uniqueId()).thenReturn(UUID.randomUUID());
        when(partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        switchServer.handleServerConnected(user, serverName);

        verify(partyProvider, never()).connectPartyToServer(party, serverName);
        verify(switchServerConfig, never()).blackEnable();
    }

    @Test
    public void expectJsonProcessingException() throws JsonProcessingException {
        final String user = "USER";
        final String serverName = "Lobby3";
        final UUID leaderUUID = UUID.randomUUID();
        final UUID partyUuid = UUID.randomUUID();
        final Party party = createParty(partyUuid, leaderUUID);

        when(userManager.getPlayer(user)).thenReturn(Optional.of(partyPlayer));
        when(partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(partyProvider.getParty(partyUuid)).thenThrow(JsonProcessingException.class);

        switchServer.handleServerConnected(user, serverName);

        verify(partyProvider, never()).connectPartyToServer(party, serverName);
    }

    private void setPartyProvider() throws IllegalAccessException {
        Field partyProviderField = ReflectionUtils.findFields(PartyAPI.class, field -> field.getType().equals(PartyProvider.class),
                TOP_DOWN).get(0);
        partyProviderField.setAccessible(true);
        partyProviderField.set(null, partyProvider);
        partyProviderField.setAccessible(false);
    }

    private @NotNull SwitchServer<String> createSwitchServer() {
        return new SwitchServer<>(userManager, switchServerConfig) {
            @Override
            public void logJsonProcessingException(final @NotNull JsonProcessingException jsonProcessingException) { }
        };
    }

    // Mockito can not Mock final classes.
    private @NotNull Party createParty(final @NotNull UUID partyId, final @NotNull UUID leader) {
        return new Party(partyId, leader, of(UUID.randomUUID()), 2);
    }
}