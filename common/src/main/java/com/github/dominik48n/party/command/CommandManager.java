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

import com.github.dominik48n.party.api.Party;
import com.github.dominik48n.party.api.player.PartyPlayer;
import com.github.dominik48n.party.config.ProxyPluginConfig;
import com.github.dominik48n.party.database.DatabaseAdapter;
import com.github.dominik48n.party.redis.RedisManager;
import com.github.dominik48n.party.utils.StringUtils;
import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class CommandManager {
   private final @NotNull Map<String, PartyCommand> commands = new HashMap<>(
         ImmutableBiMap.of(
               "invite", new InviteCommand(this, this.config().partyConfig()),
               "accept", new AcceptCommand(this, this.config().partyConfig()),
               "deny", new DenyCommand(this),
               "list", new ListCommand(this, this.config().partyConfig()),
               "leave", new LeaveCommand(this),
               "promote", new PromoteCommand(this),
               "kick", new KickCommand(this, this.redisManager())
         )
   );

   private final List<String> commandLabels = List.of(
         "invite",
         "accept",
         "deny",
         "list",
         "leave",
         "promote",
         "kick",
         "toggle"
   );

   public void addToggleCommand(final @NotNull DatabaseAdapter databaseAdapter) {
      this.commands.put("toggle", new ToggleCommand(this, databaseAdapter));

      Optional.ofNullable(this.commands.get("invite")).ifPresent(partyCommand -> {
         if (partyCommand instanceof final InviteCommand command) command.databaseAdapter(databaseAdapter);
      });
      Optional.ofNullable(this.commands.get("accept")).ifPresent(partyCommand -> {
         if (partyCommand instanceof final AcceptCommand command) command.databaseAdapter(databaseAdapter);
      });
      Optional.ofNullable(this.commands.get("leave")).ifPresent(partyCommand -> {
         if (partyCommand instanceof final LeaveCommand command) command.databaseAdapter(databaseAdapter);
      });
   }

   public void execute(final @NotNull PartyPlayer player, final @NotNull String[] args) {
      if (args.length == 0) {
         player.sendMessage("command.help");
         return;
      }

      final PartyCommand command = this.commands.get(args[0].toLowerCase());
      if (command == null) {
         player.sendMessage("command.help");
         return;
      }

      final String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
      this.runAsynchronous(() -> command.execute(player, commandArgs));
   }

   public @NotNull List<String> tabComplete(final @NotNull PartyPlayer player, final @NotNull String[] args) {
      if (args.length == 0) return Collections.emptyList();

      final String search = args[0].toLowerCase();
      if (args.length == 1 || !this.commandLabels.contains(search)) {
         return StringUtils.getSuggestions(this.commandLabels, search);
      }

      final PartyCommand command = this.commands.get(search);
      if (command == null) return Collections.emptyList();

      final String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
      return command.tabComplete(player, commandArgs);
   }


   public abstract List<String> getOnlineUserNamesAtPlayerServer(final @NotNull PartyPlayer player);

   public abstract List<String> getPartyMemberNamesAtParty(final @NotNull Party party);

   public abstract void runAsynchronous(final @NotNull Runnable runnable);

   public abstract @NotNull ProxyPluginConfig config();

   public abstract @NotNull RedisManager redisManager();
}
