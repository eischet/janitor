// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import org.jetbrains.annotations.NotNull;

/**
 * A private key with which a client signs its log-in, e.g. for the JWT grant of OAuth: the client id, the user the client
 * acts for, and the key.
 */
public interface PrivateKeyCredential extends JanitorCredential {

    /**
     * The client id, e.g. the integration key of an app.
     *
     * @return the client id
     */
    @NotNull String clientId();

    /**
     * The user the client acts for, e.g. a user id.
     *
     * @return the user, empty if the service needs none
     */
    @NotNull String subject();

    /**
     * The private key, usually in PEM format.
     *
     * @return the key
     */
    @NotNull String privateKey();

    @Override
    default @NotNull String kind() {
        return "a private key";
    }
}
