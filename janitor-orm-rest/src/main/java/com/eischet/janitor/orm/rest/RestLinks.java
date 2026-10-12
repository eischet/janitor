// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The links that the records of a join table offer, the value of {@link RestMetaData#LINKS}: one {@link RestLink} for each direction in which
 * the join table is exposed. (Meta-data keys cannot be declared for a generic list type, so this is a small class of its own.)
 */
public final class RestLinks {

    private final @NotNull List<RestLink> links;

    private RestLinks(final @NotNull List<RestLink> links) {
        this.links = List.copyOf(links);
    }

    /** @return no links */
    public static @NotNull RestLinks none() {
        return new RestLinks(List.of());
    }

    /**
     * @param link another link
     * @return a copy of these links with the given one added
     */
    public @NotNull RestLinks with(final @NotNull RestLink link) {
        final List<RestLink> result = new ArrayList<>(links);
        result.add(link);
        return new RestLinks(result);
    }

    /** @return the links, in the order in which they were declared */
    public @NotNull List<RestLink> list() {
        return links;
    }

    @Override
    public String toString() {
        return links.toString();
    }
}
