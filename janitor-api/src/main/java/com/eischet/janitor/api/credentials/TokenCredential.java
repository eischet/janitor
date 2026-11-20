// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import org.jetbrains.annotations.NotNull;

/**
 * A token that is sent as it is, e.g. as a bearer token.
 */
public interface TokenCredential extends JanitorCredential {

    /**
     * The token.
     *
     * @return the token
     */
    @NotNull String token();

    @Override
    default @NotNull String kind() {
        return "a token";
    }
}
