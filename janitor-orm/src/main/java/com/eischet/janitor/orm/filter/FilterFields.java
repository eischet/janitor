// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import com.eischet.janitor.api.types.dispatch.GenericDispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The pseudo fields of an entity that are no property: names that filters may use, each with the {@link FilterRewrite} that
 * turns a condition on it into one on real fields. Register them next to the properties, e.g.
 * <pre>{@code
 * FilterFields.of(DISPATCH)
 *         .add("createdDate@date", FilterRewrite.dateOf("createdDate"))
 *         .add("section", FilterRewrite.alias("department.section"));
 * }</pre>
 */
public final class FilterFields {

    private final GenericDispatchTable<?> owner;
    private final @Nullable FilterFields inherited;
    private final Map<String, FilterRewrite> rewrites = new ConcurrentHashMap<>();

    private FilterFields(final @NotNull GenericDispatchTable<?> owner, final @Nullable FilterFields inherited) {
        this.owner = owner;
        this.inherited = inherited;
    }

    /**
     * Returns the pseudo fields of an entity, which are kept in the meta-data of its dispatch table.
     * @param dispatch the dispatch table of the entity
     * @return the pseudo fields, to which more can be added
     */
    public static synchronized @NotNull FilterFields of(final @NotNull GenericDispatchTable<?> dispatch) {
        final FilterFields found = dispatch.getMetaData(JanitorOrm.MetaData.FILTER_FIELDS);
        if (found != null && found.owner == dispatch) {
            return found;
        }
        // none yet, or those of a parent dispatch table, which this table's fields extend but must not change
        final FilterFields fields = new FilterFields(dispatch, found);
        dispatch.setMetaData(JanitorOrm.MetaData.FILTER_FIELDS, fields);
        return fields;
    }

    /**
     * Adds a pseudo field.
     * @param name the name that filters use
     * @param rewrite how a condition on the name is rewritten
     * @return this object, to add more fields
     */
    public @NotNull FilterFields add(final @NotNull String name, final @NotNull FilterRewrite rewrite) {
        rewrites.put(name, rewrite);
        return this;
    }

    /**
     * Finds the rewrite for a field of an entity: the {@link JanitorOrm.MetaData#FILTER_REWRITE} of a property of that name, or a
     * pseudo field added here.
     * @param dispatch the dispatch table of the entity
     * @param name the field, not a path
     * @return the rewrite, or null for a real field or an unknown name
     */
    public static @Nullable FilterRewrite find(final @NotNull GenericDispatchTable<?> dispatch, final @NotNull String name) {
        final FilterRewrite ofProperty = dispatch.getMetaData(name, JanitorOrm.MetaData.FILTER_REWRITE);
        if (ofProperty != null) {
            return ofProperty;
        }
        final FilterFields fields = dispatch.getMetaData(JanitorOrm.MetaData.FILTER_FIELDS);
        return fields == null ? null : fields.get(name);
    }

    private @Nullable FilterRewrite get(final @NotNull String name) {
        final FilterRewrite rewrite = rewrites.get(name);
        return rewrite != null || inherited == null ? rewrite : inherited.get(name);
    }

}
