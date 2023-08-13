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

package com.github.dominik48n.party.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.PartyAPI;
import com.github.dominik48n.party.api.player.PartyPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class PartyCommand {
   final @NotNull CommandManager commandManager;

   PartyCommand(CommandManager commandManager) {
      this.commandManager = commandManager;
   }

   public abstract void execute(final @NotNull PartyPlayer player, final @NotNull String[] args);

   @NotNull List<String> tabComplete(final @NotNull String[] args) {
      return Collections.emptyList();
   }

   @NotNull List<String> tabComplete(final @NotNull PartyPlayer player, final @NotNull String[] args) {
      return Collections.emptyList();
   }

   @NotNull List<String> getPartyMemberNames(PartyPlayer player) {
      UUID partyID = player.partyId().orElse(null);
      if (partyID == null) return Collections.emptyList();

      Party party = null;
      try {
         party = PartyAPI.get().getParty(partyID).orElse(null);
      } catch (JsonProcessingException ignored) {

      }

      if (party == null || !party.isLeader(player.uniqueId())) return Collections.emptyList();
      List<String> partyMemberNames = this.commandManager.getPartyMemberNamesAtParty(party);

      return partyMemberNames;
   }
}
