// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.api.metadata.MetaDataKey;

/** The meta-data keys that control how entities and their properties show up in the REST API. */
public final class RestMetaData {

    private RestMetaData() {
    }

    /**
     * The path of an entity in the API, e.g. {@code "articles"}, relative to wherever the API is mounted. Set on the entity's dispatch table.
     * An entity without a path is not exposed at all.
     */
    public static final MetaDataKey<String> PATH = new MetaDataKey<>("rest_path", String.class);

    /**
     * The operations that the API offers for an entity, see {@link RestOperations}. Set on the entity's dispatch table. Absent means
     * {@link RestOperations#READ_ONLY}: writing is something that an entity has to opt in to.
     */
    public static final MetaDataKey<RestOperations> OPERATIONS = new MetaDataKey<>("rest_operations", RestOperations.class);

    /**
     * Makes changing and deleting a versioned entity (see {@code VERSION_FIELD}) depend on an {@code If-Match} header: without it, the request is
     * refused with 428. Set on the entity's dispatch table. Absent means that the header is optional: it is checked if the client sends it, and
     * otherwise the last write wins.
     */
    public static final MetaDataKey<Boolean> REQUIRE_IF_MATCH = new MetaDataKey<>("rest_require_if_match", Boolean.class);

    /**
     * The largest page size that clients may ask for; larger requests are reduced to this. Set on the entity's dispatch table.
     * Absent means {@link OrmApiModel#DEFAULT_MAX_PAGE_SIZE}.
     */
    public static final MetaDataKey<Integer> MAX_PAGE_SIZE = new MetaDataKey<>("rest_max_page_size", Integer.class);

    /**
     * The default sort order of lists, as a comma-separated list of field names, each optionally prefixed by {@code -} for descending,
     * e.g. {@code "name,-stock"}. Set on the entity's dispatch table. Absent means the DAO's own default.
     */
    public static final MetaDataKey<String> DEFAULT_SORT = new MetaDataKey<>("rest_default_sort", String.class);

    /**
     * Makes the entities that refer to another entity through a reference property show up below that entity: set on the reference property of
     * the <em>child</em>, e.g. {@code "docs"} on the property {@code folder} of {@code Doc}, which exposes {@code /folders/{id}/docs}. Both
     * entities have to be exposed. Without it, there is no such path.
     */
    public static final MetaDataKey<String> SUB_PATH = new MetaDataKey<>("rest_sub_path", String.class);

    /**
     * For a sub-resource ({@link #SUB_PATH}) on a property that holds the id of the parent as a plain number instead of a reference: the name
     * of the parent entity in its collection. A reference property knows its target itself and does not need this.
     */
    public static final MetaDataKey<String> SUB_PARENT = new MetaDataKey<>("rest_sub_parent", String.class);

    /**
     * Exposes the records of a join table as links between the entities that it connects, see {@link RestLinks}. Set on the dispatch table of
     * the join records.
     */
    public static final MetaDataKey<RestLinks> LINKS = new MetaDataKey<>("rest_links", RestLinks.class);

    /** Set on a property to keep it out of the API entirely, e.g. for internal fields. */
    public static final MetaDataKey<Boolean> HIDDEN = new MetaDataKey<>("rest_hidden", Boolean.class);

    /**
     * Set on a property to allow or forbid filtering by it. Absent means: allowed for database-backed properties except long texts and lazily
     * loaded ones.
     */
    public static final MetaDataKey<Boolean> FILTERABLE = new MetaDataKey<>("rest_filterable", Boolean.class);

    /** Set on a property to allow or forbid sorting by it; the default is the same as for {@link #FILTERABLE}. */
    public static final MetaDataKey<Boolean> SORTABLE = new MetaDataKey<>("rest_sortable", Boolean.class);

    /** An example value for a property, as it would appear in JSON text, for the API description. */
    public static final MetaDataKey<String> EXAMPLE = new MetaDataKey<>("rest_example", String.class);

}
