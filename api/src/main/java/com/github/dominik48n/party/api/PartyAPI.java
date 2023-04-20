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

package com.github.dominik48n.party.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main class for interacting with the Party API.
 */
public class PartyAPI {

    private static @Nullable PartyProvider partyProvider = null;

    @ApiStatus.Internal
    private PartyAPI() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the instance of the {@link PartyProvider} for interacting with the Party API.
     *
     * @return The instance of the {@link PartyProvider}.
     *
     * @throws IllegalStateException if the Party API is not initialized.
     */
    public static @NotNull PartyProvider get() throws IllegalStateException {
        if (partyProvider == null) throw new IllegalStateException("PartyAPI isn't initialized.");
        return partyProvider;
    }

    /**
     * Sets the internal instance of the {@link PartyProvider}.
     *
     * @param provider The instance of the {@link PartyProvider}.
     */
    @ApiStatus.Internal
    static void set(final @NotNull PartyProvider provider) {
        partyProvider = provider;
    }
}
