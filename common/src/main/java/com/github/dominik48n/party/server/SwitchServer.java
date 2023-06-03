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
    private final @NotNull SwitchServerConfig config;

    public SwitchServer(final @NotNull  UserManager<TUser> userManager, final @NotNull  SwitchServerConfig switchServerConfig) {
        this.userManager = userManager;
        this.config = switchServerConfig;
    }

    public abstract void logJsonProcessingException(final @NotNull JsonProcessingException jsonProcessingException);

    protected void handleServerConnected(final @NotNull TUser user, final @NotNull String serverName) {
        this.userManager.getPlayer(user)
                .flatMap(this::handlePartyPlayer)//Get party first
                .filter(party -> allowServerSwitch(serverName))//Then check regex. Could be more expensive.
                .ifPresent(party -> connectPartyToServer(serverName, party));
    }

    private static void connectPartyToServer(final @NotNull String serverName, final @NotNull Party party) {
        get().connectPartyToServer(party, serverName);
    }

    private @NotNull Optional<Party> handlePartyPlayer(final @NotNull PartyPlayer player) {
        return player.partyId()
                .flatMap(this::getParty)
                .filter(party -> party.isLeader(player.uniqueId()));
    }

    private @NotNull Optional<Party> getParty(UUID uuid) {
        try {
            return get().getParty(uuid);
        } catch (JsonProcessingException jsonProcessingException) {
            logJsonProcessingException(jsonProcessingException);
            return Optional.empty();
        }
    }

    private boolean allowServerSwitch(String serverName) {
        //Regex Pattern could be expensive. Maybe cache results.
        if (this.config.blackEnable() &&
                match(this.config.blackPatternList(), serverName)) {
            return this.config.whiteEnable() && match(this.config.whitePatternList(), serverName);
        }

        return true;
    }

    private boolean match(List<Pattern> patternList, String serverName) {
        return patternList.stream()
                .map(pattern -> pattern.matcher(serverName))
                .anyMatch(Matcher::matches);
    }

}
