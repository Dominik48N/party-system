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
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.SwitchServerConfig;
import com.github.dominik48n.party.user.UserManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dominik48n.party.api.PartyAPI.get;

public abstract class SwitchServer<TUser> {

    private final @NotNull UserManager<TUser> userManager;
    private final @NotNull SwitchServerConfig switchServerConfig;

    public SwitchServer(@NotNull UserManager<TUser> userManager, @NotNull SwitchServerConfig switchServerConfig) {
        this.userManager = userManager;
        this.switchServerConfig = switchServerConfig;
    }

    public abstract void logJsonProcessingException(JsonProcessingException jsonProcessingException);

    protected void handleServerConnected(TUser user, String serverName) {
        userManager.getPlayer(user)
                .flatMap(this::handlePartyPlayer)//Get party first
                .filter(party -> allowServerSwitch(serverName))//Then check regex. Could be more expensive.
                .ifPresent(party -> connectPartyToServer(serverName, party));
    }

    private static void connectPartyToServer(String serverName, Party party) {
        get().connectPartyToServer(party, serverName);
    }

    @NotNull
    private Optional<Party> handlePartyPlayer(PartyPlayer player) {
        return player.partyId()
                .flatMap(this::getParty)
                .filter(party -> isPartyLeader(player, party));
    }

    private static boolean isPartyLeader(PartyPlayer player, Party party) {
        return party.isLeader(player.uniqueId());
    }

    @NotNull
    private Optional<Party> getParty(UUID uuid) {
        try {
            return get().getParty(uuid);
        } catch (JsonProcessingException jsonProcessingException) {
            logJsonProcessingException(jsonProcessingException);
            return Optional.empty();
        }
    }

    private boolean allowServerSwitch(String serverName) {
        //Regex Pattern could be expensive. Maybe cache results.
        if (switchServerConfig.blackEnable() && match(switchServerConfig.blackPatternList(), serverName)) {
            return switchServerConfig.whiteEnable() && match(this.switchServerConfig.whitePatternList(), serverName);
        }

        return true;
    }

    private boolean match(List<Pattern> patternList, String serverName) {
        return patternList.stream()
                .map(pattern -> pattern.matcher(serverName))
                .anyMatch(Matcher::matches);
    }

}
