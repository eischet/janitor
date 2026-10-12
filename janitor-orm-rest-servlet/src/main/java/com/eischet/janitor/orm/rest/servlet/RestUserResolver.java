// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest.servlet;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;

/**
 * Finds out who is calling, for a request. The result is whatever the host's {@link com.eischet.janitor.orm.rest.RestPolicy} wants to work with,
 * typed as the host likes: the class of the people that the host knows, a name, a token. The servlet does not look at the container's own
 * security model ({@code getUserPrincipal()}, {@code isUserInRole()}); if the host wants those, it can use them in here.
 *
 * @param <U> the type of the user
 */
@FunctionalInterface
public interface RestUserResolver<U> {

    /**
     * @param request the request
     * @return the caller, as the policy wants it, or null for an anonymous caller
     */
    @Nullable U resolve(HttpServletRequest request);

    /**
     * @param <U> the type of the user
     * @return a resolver that always answers "anonymous"
     */
    static <U> RestUserResolver<U> anonymous() {
        return request -> null;
    }
}
