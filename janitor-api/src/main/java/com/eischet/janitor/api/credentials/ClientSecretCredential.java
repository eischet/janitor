// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import org.jetbrains.annotations.NotNull;

/**
 * A client id and its secret, e.g. for the OAuth client credentials flow.
 */
public interface ClientSecretCredential extends JanitorCredential {

    /**
     * The client id.
     *
     * @return the client id
     */
    @NotNull String clientId();

    /**
     * The client secret.
     *
     * @return the secret
     */
    @NotNull String clientSecret();

    @Override
    default @NotNull String kind() {
        return "a client id and secret";
    }
}
