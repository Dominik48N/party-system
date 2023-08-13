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
import com.github.dominik48n.party.config.PartyConfig;
import com.github.dominik48n.party.database.DatabaseAdapter;
import com.github.dominik48n.party.database.settings.DatabaseSettingsType;
import com.github.dominik48n.party.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AcceptCommand extends PartyCommand {
   AcceptCommand(CommandManager commandManager, @NotNull PartyConfig config) {
      super(commandManager);
      this.config = config;
   }

   private final @NotNull PartyConfig config;

   private @Nullable DatabaseAdapter databaseAdapter;

   @Override
   public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
      if (args.length != 1) {
         player.sendMessage("command.usage.accept");
         return;
      }

      if (player.partyId().isPresent()) {
         player.sendMessage("command.accept.already");
         return;
      }

      final String name = args[0];
      if (!PartyAPI.get().existsPartyRequest(name, player.name())) {
         player.sendMessage("command.accept.no_request");
         return;
      }

      PartyAPI.get().removePartyRequest(name, player.name());

      Optional<PartyPlayer> target;
      try {
         target = PartyAPI.get().onlinePlayerProvider().get(name);
      } catch (final JsonProcessingException e) {
         target = Optional.empty();
      }
      if (target.isEmpty() || target.get().partyId().isEmpty()) {
         player.sendMessage("command.accept.no_request");
         return;
      }

      Optional<Party> party;
      try {
         party = PartyAPI.get().getParty(target.get().partyId().get());
      } catch (final JsonProcessingException e) {
         party = Optional.empty();
      }
      if (party.isEmpty()) {
         player.sendMessage("command.accept.no_request");
         return;
      }

      if (this.config.useMemberLimit() && party.get().members().size() >= party.get().maxMembers()) {
         player.sendMessage("command.accept.limit");
         return;
      }

      try {
         PartyAPI.get().addPlayerToParty(party.get().id(), player.uniqueId());
      } catch (final JsonProcessingException e) {
         player.sendMessage("general.error");
         return;
      }

      final List<UUID> playersToMessage = this.databaseAdapter != null ?
            this.databaseAdapter.getPlayersWithEnabledSetting(party.get().allMembers(), DatabaseSettingsType.NOTIFICATIONS) :
            party.get().allMembers();
      PartyAPI.get().sendMessageToPlayers(playersToMessage, "party.join", player.name());

      player.partyId(party.get().id());
      player.sendMessage("command.accept.joined");
   }

   void databaseAdapter(final @NotNull DatabaseAdapter databaseAdapter) {
      this.databaseAdapter = databaseAdapter;
   }

   @Override
   @NotNull List<String> tabComplete(@NotNull PartyPlayer player, @NotNull String[] args) {
      if (args.length > 1) return Collections.emptyList();
      List<String> suggestions = StringUtils.getSuggestions(this.commandManager.getOnlineUserNamesAtPlayerServer(player), args[0]);
      suggestions.remove(player.name());

      return suggestions;
   }
}
