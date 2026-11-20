// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.credentials;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Helpers for modules that accept credentials by name.
 */
public final class JanitorCredentials {

    private JanitorCredentials() {
    }

    /**
     * Looks up the credentials a script named and checks that they are of a kind the caller can use.
     *
     * @param process the running script
     * @param name    the name the script passed
     * @param use     what the credentials are for, for error messages, e.g. "httpclient.credential"
     * @param kinds   the kinds the caller accepts, at least one
     * @return the credentials, an instance of one of the kinds
     * @throws JanitorRuntimeException if the host offers no credentials, there are none by that name, the script must not use
     *                                 them, or they are of another kind
     */
    public static @NotNull JanitorCredential require(final @NotNull JanitorScriptProcess process, final @NotNull String name, final @NotNull String use,
                                                     final @NotNull List<Class<? extends JanitorCredential>> kinds) throws JanitorRuntimeException {
        final JanitorCredentialProvider provider = process.getEnvironment().getCredentialProvider();
        if (provider == null) {
            throw new JanitorArgumentException(process, use + ": no credentials are available here");
        }
        final JanitorCredential credential = provider.resolve(process, name);
        if (credential == null) {
            throw new JanitorArgumentException(process, use + ": credentials " + name + " do not exist");
        }
        for (final Class<? extends JanitorCredential> kind : kinds) {
            if (kind.isInstance(credential)) {
                return credential;
            }
        }
        throw new JanitorArgumentException(process, use + ": credentials " + name + " hold " + credential.kind() + ", which cannot be used here");
    }

    /**
     * Like {@link #require(JanitorScriptProcess, String, String, List)} for exactly one kind.
     *
     * @param process the running script
     * @param name    the name the script passed
     * @param use     what the credentials are for, for error messages
     * @param kind    the kind the caller needs
     * @param <T>     the kind
     * @return the credentials
     * @throws JanitorRuntimeException as for the general form
     */
    public static <T extends JanitorCredential> @NotNull T require(final @NotNull JanitorScriptProcess process, final @NotNull String name, final @NotNull String use,
                                                                   final @NotNull Class<T> kind) throws JanitorRuntimeException {
        return kind.cast(require(process, name, use, List.of(kind)));
    }
}
