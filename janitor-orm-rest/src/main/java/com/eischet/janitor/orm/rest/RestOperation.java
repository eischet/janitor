// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

/** What a request wants to do with an entity; this is what a {@link RestPolicy} decides about. */
public enum RestOperation {
    /** Lists entities, with filter, sort order and paging, by {@code GET /{path}}. */
    LIST,
    /** Like {@link #LIST}, but with the parameters in the body of a {@code POST /{path}/search}. */
    SEARCH,
    /** Gets one entity by id or by key. */
    GET,
    /** Creates an entity, by {@code POST /{path}}. */
    CREATE,
    /** Changes an entity, by {@code PUT} (replaces all properties) or {@code PATCH} (changes the given ones) of {@code /{path}/{id}}. */
    UPDATE,
    /** Deletes an entity by id, with {@code DELETE /{path}/{id}}. */
    DELETE;

    /** @return true if this operation only reads */
    public boolean isRead() {
        return this == LIST || this == SEARCH || this == GET;
    }
}
