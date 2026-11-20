// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up the credentials that a script names; the host returns it from
 * {@link com.eischet.janitor.api.JanitorEnvironment#getCredentialProvider()} and decides which scripts may use which credentials.
 */
@FunctionalInterface
public interface JanitorCredentialProvider {

    /**
     * Looks up credentials.
     *
     * @param process the running script that asks for them
     * @param name    the name the script passed
     * @return the credentials, or null if there are none by that name
     * @throws JanitorRuntimeException if the script must not use them, or they cannot be read
     */
    @Nullable JanitorCredential resolve(@NotNull JanitorScriptProcess process, @NotNull String name) throws JanitorRuntimeException;
}
