// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import org.jetbrains.annotations.NotNull;

/**
 * Credentials that the host keeps for scripts: a script refers to them by name, and a module reads the secret through one of
 * the sub-interfaces, e.g. {@link UsernamePasswordCredential}. Scripts never see the secret, so it cannot end up in script
 * code, output or logs.
 * <p>
 * Implementations are plain Java objects, not script values. A host should read the secret only when a getter is called,
 * so that a missing key or a similar problem shows up where the credentials are used.
 * </p>
 *
 * @see JanitorCredentialProvider
 * @see JanitorCredentials
 */
public interface JanitorCredential {

    /**
     * The name by which scripts refer to these credentials.
     *
     * @return the name
     */
    @NotNull String name();

    /**
     * What these credentials hold, for error messages, e.g. "a user name and password".
     *
     * @return a short description
     */
    @NotNull String kind();
}
