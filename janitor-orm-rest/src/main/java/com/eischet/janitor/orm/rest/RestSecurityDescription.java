// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * What the host tells the API description about who may call which operation, in terms of security schemes. It is purely descriptive: the
 * {@link RestPolicy} decides for every request, whatever is said here. The answers say what tools should show and what clients should send;
 * they do not depend on a user.
 * <p>
 * {@link RestPolicy} implements this with defaults that say nothing, so that the description's default applies everywhere (see
 * {@link OpenApiOptions#withSecurity}); override these methods to deviate, e.g. for operations that are public or need other scopes.
 * </p>
 */
public interface RestSecurityDescription {

    /**
     * @param entity    an entity
     * @param operation an operation on it
     * @return the alternatives of which one is needed to do it: {@code null} for "as the description says by default", an empty list for
     * "nothing is needed, it is public"
     */
    default @Nullable List<ApiSecurityRequirement> securityFor(final @NotNull ApiEntity entity, final @NotNull RestOperation operation) {
        return null;
    }

    /**
     * @param link      a link
     * @param operation an operation on it
     * @return like {@link #securityFor}; by default what the entity that the link belongs to needs for getting (reading) or updating (writing)
     */
    default @Nullable List<ApiSecurityRequirement> securityForLink(final @NotNull ApiLink link, final @NotNull RestOperation operation) {
        return securityFor(link.parent(), operation.isRead() ? RestOperation.GET : RestOperation.UPDATE);
    }

    /** @return a description that says nothing */
    static @NotNull RestSecurityDescription none() {
        return new RestSecurityDescription() {
        };
    }
}
