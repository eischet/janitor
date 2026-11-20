// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import org.jetbrains.annotations.NotNull;

/**
 * A user name and a password, e.g. for HTTP basic authentication.
 */
public interface UsernamePasswordCredential extends JanitorCredential {

    /**
     * The user name.
     *
     * @return the user name
     */
    @NotNull String username();

    /**
     * The password.
     *
     * @return the password, empty if there is none
     */
    @NotNull String password();

    @Override
    default @NotNull String kind() {
        return "a user name and password";
    }
}
