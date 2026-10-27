// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.meta.OrmPropertyHandle;
import org.jetbrains.annotations.NotNull;

/** Fluent helpers for setting the {@link RestMetaData} of entities and properties. */
public final class RestOrm {

    private RestOrm() {
    }

    /**
     * Exposes an entity in the API.
     * @param table the dispatch table of the entity
     * @param path  the path of the entity, e.g. "articles"; no leading or trailing slash
     * @return the table
     */
    public static <T extends OrmEntity, U extends Uplink> EntityDispatchTable<T, U> expose(final @NotNull EntityDispatchTable<T, U> table, final @NotNull String path) {
        checkPath(path);
        table.setMetaData(RestMetaData.PATH, path);
        return table;
    }

    private static void checkPath(final String path) {
        if (path.isBlank() || path.startsWith("/") || path.endsWith("/") || path.contains("{") || path.contains("/")) {
            throw new IllegalArgumentException("invalid REST path '" + path + "'");
        }
    }

    /**
     * Exposes the entities that refer to another entity through a reference property below that entity: given the property {@code folder} of
     * {@code Doc}, {@code subresource(handle, "docs")} makes {@code /folders/{id}/docs} list the docs of a folder, and create docs in it.
     * Both entities have to be exposed.
     * @param handle the reference property of the child
     * @param path   the path below the parent, e.g. "docs"
     * @return the handle
     */
    public static <T, V> OrmPropertyHandle<T, V> subresource(final @NotNull OrmPropertyHandle<T, V> handle, final @NotNull String path) {
        checkPath(path);
        return handle.setMetaData(RestMetaData.SUB_PATH, path);
    }

    /**
     * Exposes the records of a join table as links, in one direction: {@code link(membership, "user", "groups", "group")} makes
     * {@code /users/{id}/groups} the memberships of a user, and {@code /users/{id}/groups/{groupId}} one of them, which can be created with PUT and
     * deleted (if the join table allows {@link RestOperation#CREATE} and {@link RestOperation#DELETE}). Call it again for the other direction.
     * Both entities have to be exposed, and the primary key of the join table must contain the two columns of the references. If it has more
     * columns (e.g. a reason, so that two entities can be linked more than once), the links can only be listed. Links can only be created
     * if the join table is an {@link OrmDispatchTable}, which can create new records.
     *
     * @param joinTable the dispatch table of the join records
     * @param fromField the property of the join records that refers to the entity that the links belong to
     * @param path      the path below that entity, e.g. "groups"
     * @param toField   the property of the join records that refers to the entity at the other end
     * @return the join table
     */
    public static <D extends DispatchTable<?>> D link(final @NotNull D joinTable, final @NotNull String fromField, final @NotNull String path, final @NotNull String toField) {
        checkPath(path);
        final RestLinks current = joinTable.getMetaData(RestMetaData.LINKS);
        joinTable.setMetaData(RestMetaData.LINKS, (current == null ? RestLinks.none() : current).with(new RestLink(fromField, path, toField)));
        return joinTable;
    }

    /**
     * Offers operations in addition to the reading ones, e.g. {@code RestOrm.allow(table, RestOperation.CREATE, RestOperation.UPDATE)}.
     * Allowing an operation only makes it possible: the {@link RestPolicy} still decides for every request.
     * @param table      the dispatch table of the entity
     * @param operations the operations to offer
     * @return the table
     */
    public static <D extends OrmDispatchTable<?, ?>> D allow(final @NotNull D table, final @NotNull RestOperation... operations) {
        final RestOperations current = table.getMetaData(RestMetaData.OPERATIONS);
        table.setMetaData(RestMetaData.OPERATIONS, (current == null ? RestOperations.READ_ONLY : current).with(operations));
        return table;
    }

    /**
     * Offers exactly the given operations, replacing the default ({@link RestOperations#READ_ONLY}).
     * @param table      the dispatch table of the entity
     * @param operations the operations to offer
     * @return the table
     */
    public static <D extends OrmDispatchTable<?, ?>> D operations(final @NotNull D table, final @NotNull RestOperation... operations) {
        table.setMetaData(RestMetaData.OPERATIONS, RestOperations.of(operations));
        return table;
    }

    /**
     * Makes {@code PUT}, {@code PATCH} and {@code DELETE} of a versioned entity require an {@code If-Match} header with the version that the
     * client has seen (without it: 428). By default, the header is optional: it is checked if sent, and otherwise the last write wins.
     * @param table the dispatch table of the entity
     * @return the table
     */
    public static <T extends OrmEntity, U extends Uplink> EntityDispatchTable<T, U> requireIfMatch(final @NotNull EntityDispatchTable<T, U> table) {
        table.setMetaData(RestMetaData.REQUIRE_IF_MATCH, Boolean.TRUE);
        return table;
    }

    /** Keeps a property out of the API. */
    public static <T, V> OrmPropertyHandle<T, V> hidden(final @NotNull OrmPropertyHandle<T, V> handle) {
        return handle.setMetaData(RestMetaData.HIDDEN, Boolean.TRUE);
    }

    /** Forbids filtering by a property. */
    public static <T, V> OrmPropertyHandle<T, V> notFilterable(final @NotNull OrmPropertyHandle<T, V> handle) {
        return handle.setMetaData(RestMetaData.FILTERABLE, Boolean.FALSE);
    }

    /** Forbids sorting by a property. */
    public static <T, V> OrmPropertyHandle<T, V> notSortable(final @NotNull OrmPropertyHandle<T, V> handle) {
        return handle.setMetaData(RestMetaData.SORTABLE, Boolean.FALSE);
    }

    /** Gives an example value for the API description, as JSON text, e.g. {@code "\"ABC-1\""} or {@code "42"}. */
    public static <T, V> OrmPropertyHandle<T, V> example(final @NotNull OrmPropertyHandle<T, V> handle, final @NotNull String jsonExample) {
        return handle.setMetaData(RestMetaData.EXAMPLE, jsonExample);
    }
}
