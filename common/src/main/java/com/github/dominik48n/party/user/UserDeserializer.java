package com.github.dominik48n.party.user;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dominik48n.party.api.player.PartyPlayer;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserDeserializer extends JsonDeserializer<PartyPlayer> {

    @Override
    public PartyPlayer deserialize(final JsonParser parser, DeserializationContext ctxt) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        final UUID uniqueId = UUID.fromString(node.get("uuid").asText());
        final String name = node.get("name").asText();
        final UUID partyId = node.hasNonNull("party_id") ? UUID.fromString(node.get("party_id").asText()) : null;
        return new DeserializedUser(uniqueId, name, partyId);
    }

    private static class DeserializedUser implements PartyPlayer {

        private final @NotNull UUID uniqueId;
        private final @NotNull String name;
        private @Nullable UUID partyId;

        public DeserializedUser(final @NotNull UUID uniqueId, final @NotNull String name, final @Nullable UUID partyId) {
            this.uniqueId = uniqueId;
            this.name = name;
            this.partyId = partyId;
        }

        @Override
        public @NotNull UUID uniqueId() {
            return this.uniqueId;
        }

        @Override
        public @NotNull String name() {
            return this.name;
        }

        @Override
        public @NotNull Optional<UUID> partyId() {
            return Optional.ofNullable(this.partyId);
        }

        @Override
        public void partyId(final @Nullable UUID partyId) {
            this.partyId = partyId;
        }

        @Override
        public void sendMessage(final @NotNull String messageKey, final @NotNull Object... replacements) {
            throw new UnsupportedOperationException();
        }
    }
}