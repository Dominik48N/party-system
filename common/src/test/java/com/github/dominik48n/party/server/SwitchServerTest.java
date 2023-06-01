package com.github.dominik48n.party.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.PartyProvider;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.SwitchServerConfig;
import com.github.dominik48n.party.user.UserManager;
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
        String user = "USER";
        String serverName = "Lobby3";
        UUID leaderUUID = UUID.randomUUID();
        UUID partyUuid = UUID.randomUUID();
        Party party = createParty(partyUuid, leaderUUID);

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
    void handleServerConnectedFailBecauseNotMatchWhiteList() throws JsonProcessingException {
        String user = "USER";
        String serverName = "Lobby2";
        UUID leaderUUID = UUID.randomUUID();
        UUID partyUuid = UUID.randomUUID();
        Party party = createParty(partyUuid, leaderUUID);

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
    void handleServerConnectedDisableWhiteList() throws JsonProcessingException {
        String user = "USER";
        String serverName = "Lobby3";
        UUID leaderUUID = UUID.randomUUID();
        UUID partyUuid = UUID.randomUUID();
        Party party = createParty(partyUuid, leaderUUID);

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
    void handleServerConnectedDisableBlackList() throws JsonProcessingException {
        String user = "USER";
        String serverName = "Lobby3";
        UUID leaderUUID = UUID.randomUUID();
        UUID partyUuid = UUID.randomUUID();
        Party party = createParty(partyUuid, leaderUUID);

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
    void handleServerConnectedNotPartyLeader() throws JsonProcessingException {
        String user = "USER";
        String serverName = "Lobby3";
        UUID leaderUUID = UUID.randomUUID();
        UUID partyUuid = UUID.randomUUID();
        Party party = createParty(partyUuid, leaderUUID);

        when(userManager.getPlayer(user)).thenReturn(Optional.of(partyPlayer));
        when(partyPlayer.partyId()).thenReturn(Optional.of(partyUuid));
        when(partyPlayer.uniqueId()).thenReturn(UUID.randomUUID());
        when(partyProvider.getParty(partyUuid)).thenReturn(Optional.of(party));

        switchServer.handleServerConnected(user, serverName);

        verify(partyProvider, never()).connectPartyToServer(party, serverName);
        verify(switchServerConfig, never()).blackEnable();
    }

    @Test
    public void expectError() throws JsonProcessingException {
        String user = "USER";
        String serverName = "Lobby3";
        UUID leaderUUID = UUID.randomUUID();
        UUID partyUuid = UUID.randomUUID();
        Party party = createParty(partyUuid, leaderUUID);

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

    private SwitchServer<String> createSwitchServer() {
        return new SwitchServer<>(userManager, switchServerConfig) {
            @Override
            public void logJsonProcessingException(JsonProcessingException jsonProcessingException) { }
        };
    }

    //Mockito can not Mock final classes.
    private Party createParty(UUID partyUuid, UUID leaderUUID) {
        return new Party(partyUuid, leaderUUID, of(UUID.randomUUID()), 2);
    }
}