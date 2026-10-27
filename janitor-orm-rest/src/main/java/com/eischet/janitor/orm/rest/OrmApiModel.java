// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.GenericDispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.meta.OrmPropertyHandle;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes which entities of a {@link OrmDaoCollection} the REST API exposes, and how: the single source for both the API description
 * and the request handling. It is built from the meta-data of the entities' dispatch tables (see {@link RestMetaData} and
 * {@link JanitorOrm.MetaData}) and does not change afterwards, so it can be built once and shared.
 * <p>
 * An entity is exposed if and only if its dispatch table has a {@link RestMetaData#PATH}. Of its properties, the API exposes those that are
 * backed by a database column, are not {@link RestMetaData#HIDDEN} and exist in the schema version of the collection.
 * </p>
 */
public final class OrmApiModel {

    /** The largest page size, unless an entity says otherwise with {@link RestMetaData#MAX_PAGE_SIZE}. */
    public static final int DEFAULT_MAX_PAGE_SIZE = 500;

    /** The page size if the client does not ask for one. */
    public static final int DEFAULT_PAGE_SIZE = 50;

    private final @NotNull @Unmodifiable List<ApiEntity> entities;
    private final @NotNull @Unmodifiable List<ApiSubresource> subresources;
    private final @NotNull @Unmodifiable List<ApiLink> links;

    private OrmApiModel(final @NotNull List<ApiEntity> entities, final @NotNull List<ApiSubresource> subresources, final @NotNull List<ApiLink> links) {
        this.entities = List.copyOf(entities);
        this.subresources = List.copyOf(subresources);
        this.links = List.copyOf(links);
    }

    /**
     * Builds the model of a collection.
     * @param collection the collection whose DAOs are looked at
     * @return the model
     */
    public static @NotNull OrmApiModel of(final @NotNull OrmDaoCollection<?> collection) {
        return build(collection, null);
    }

    /**
     * Builds the model of a part of a collection, e.g. for one of several APIs on the same database that offer different entities: only the
     * exposed entities that the selection names are part of it, with the operations the selection gives them instead of
     * {@link RestMetaData#OPERATIONS}. Sub-resources and links whose entities are not both part of the model are left out (in the full model,
     * that is an error in the declarations).
     * @param collection the collection whose DAOs are looked at
     * @param selection  which entities to take, and what they offer
     * @return the model
     */
    public static @NotNull OrmApiModel of(final @NotNull OrmDaoCollection<?> collection, final @NotNull ApiSelection selection) {
        return build(collection, selection);
    }

    private static @NotNull OrmApiModel build(final @NotNull OrmDaoCollection<?> collection, final @Nullable ApiSelection selection) {
        final List<ApiEntity> result = new ArrayList<>();
        for (final Dao<? extends OrmEntity> dao : collection.getDaos()) {
            final EntityDispatchTable<?, ?> table = collection.getEntityDispatchTable(dao.getEntityClassName());
            if (table == null) {
                continue;
            }
            final String path = table.getMetaData(RestMetaData.PATH);
            if (path == null || path.isBlank()) {
                continue;
            }
            final RestOperations operations;
            if (selection == null) {
                operations = table.getMetaData(RestMetaData.OPERATIONS);
            } else {
                operations = selection.operationsFor(dao.getEntityClassName());
                if (operations == null) {
                    continue;
                }
            }
            result.add(buildEntity(collection, dao.getEntityClassName(), path, table, operations));
        }
        final boolean partial = selection != null;
        final List<ApiSubresource> subresources = buildSubresources(result, partial);
        final List<ApiLink> links = buildLinks(collection, result, partial);
        checkUniquePaths(subresources, links);
        return new OrmApiModel(result, subresources, links);
    }

    private static ApiEntity entityNamed(final List<ApiEntity> entities, final String className) {
        for (final ApiEntity entity : entities) {
            if (entity.className().equals(className)) {
                return entity;
            }
        }
        return null;
    }

    private static List<ApiSubresource> buildSubresources(final List<ApiEntity> entities, final boolean partial) {
        final List<ApiSubresource> result = new ArrayList<>();
        for (final ApiEntity child : entities) {
            for (final ApiField field : child.fields()) {
                final String path = child.table().getMetaData(field.name(), RestMetaData.SUB_PATH);
                if (path == null) {
                    continue;
                }
                if (!field.isReference()) {
                    throw new IllegalStateException("the sub-resource '" + path + "' is declared on " + child.className() + "." + field.name() + ", which is not a reference");
                }
                final ApiEntity parent = entityNamed(entities, field.referencedEntity());
                if (parent == null && partial) {
                    continue;
                }
                if (parent == null) {
                    throw new IllegalStateException("the sub-resource '" + path + "' of " + child.className() + "." + field.name() + " is below " + field.referencedEntity() + ", which is not exposed");
                }
                result.add(new ApiSubresource(path, parent, child, field));
            }
        }
        return result;
    }

    private static List<ApiLink> buildLinks(final OrmDaoCollection<?> collection, final List<ApiEntity> entities, final boolean partial) {
        final List<ApiLink> result = new ArrayList<>();
        for (final String joinName : collection.getJoinNames()) {
            final JoinDao<? extends OrmJoined> dao = collection.getJoinDao(joinName);
            if (dao == null) {
                continue;
            }
            final DispatchTable<?> table = dao.getEntityDispatchTable();
            final RestLinks declared = table.getMetaData(RestMetaData.LINKS);
            if (declared == null) {
                continue;
            }
            final RestOperations declaredOperations = table.getMetaData(RestMetaData.OPERATIONS);
            final Set<RestOperation> operations = declaredOperations == null ? RestOperations.READ_ONLY.set() : declaredOperations.set();
            for (final RestLink link : declared.list()) {
                if (partial && !connectsExposed(entities, table, link)) {
                    continue;
                }
                result.add(buildLink(collection, entities, dao, joinName, table, link, operations));
            }
        }
        return result;
    }

    /**
     * Whether both ends of a link are entities of the model; used to leave out links of a partial model.
     */
    private static boolean connectsExposed(final List<ApiEntity> entities, final DispatchTable<?> table, final RestLink link) {
        final String from = table.getMetaData(link.fromField(), Janitor.MetaData.REF);
        final String to = table.getMetaData(link.toField(), Janitor.MetaData.REF);
        return from != null && to != null && entityNamed(entities, from) != null && entityNamed(entities, to) != null;
    }

    private static ApiLink buildLink(final OrmDaoCollection<?> collection, final List<ApiEntity> entities, final JoinDao<? extends OrmJoined> dao, final String joinName,
                                     final DispatchTable<?> table, final RestLink link, final Set<RestOperation> operations) {
        final String where = "the link '" + link.path() + "' of " + joinName;
        final List<ApiField> fields = new ArrayList<>();
        for (final String name : table.streamAttributeNames().toList()) {
            final String column = table.getMetaData(name, JanitorOrm.MetaData.COLUMN_NAME);
            final ColumnTypeHint columnType = table.getMetaData(name, JanitorOrm.MetaData.COLUMN_TYPE);
            if (column == null || column.isBlank() || columnType == null || Boolean.TRUE.equals(table.getMetaData(name, RestMetaData.HIDDEN))) {
                continue;
            }
            if (!OrmPropertyHandle.isAvailableIn(table.getMetaData(name, JanitorOrm.MetaData.VERSION_RANGE), collection.getSchemaVersion())) {
                continue;
            }
            final boolean pointsAlong = name.equals(link.fromField()) || name.equals(link.toField());
            fields.add(buildField(collection, table, name, column, columnType, pointsAlong));
        }
        final ApiField from = fields.stream().filter(field -> field.name().equals(link.fromField())).findFirst().orElse(null);
        final ApiField to = fields.stream().filter(field -> field.name().equals(link.toField())).findFirst().orElse(null);
        if (from == null || to == null || !from.isReference() || !to.isReference() || from.name().equals(to.name())) {
            throw new IllegalStateException(where + " needs two different reference properties '" + link.fromField() + "' and '" + link.toField() + "'");
        }
        final ApiEntity parent = entityNamed(entities, from.referencedEntity());
        final ApiEntity other = entityNamed(entities, to.referencedEntity());
        if (parent == null || other == null) {
            throw new IllegalStateException(where + " connects " + from.referencedEntity() + " and " + to.referencedEntity() + ", and both have to be exposed");
        }
        final Set<String> key = Set.copyOf(dao.getPrimaryKeyColumns());
        if (!key.contains(from.column()) || !key.contains(to.column())) {
            throw new IllegalStateException(where + " cannot address its records: the primary key is " + dao.getPrimaryKeyColumns() + ", but has to contain " + from.column() + " and " + to.column());
        }
        // with more columns in the key (e.g. a reason), two entities can be linked more than once: such links can only be listed
        final Set<RestOperation> offered = key.size() == 2 ? operations : operations.contains(RestOperation.LIST) ? Set.of(RestOperation.LIST) : Set.of();
        if (offered.contains(RestOperation.CREATE) && !(table instanceof OrmDispatchTable<?, ?>)) {
            throw new IllegalStateException(where + " needs a dispatch table that can create new records, an OrmDispatchTable");
        }
        final String className = table.getMetaData(Janitor.MetaData.CLASS);
        return new ApiLink(link.path(), parent, other, className == null ? joinName : className, table, List.copyOf(fields),
                from.name(), to.name(), from.column(), to.column(), offered);
    }

    /** Below an entity, sub-resources and links share the paths, so they must not use one twice. */
    private static void checkUniquePaths(final List<ApiSubresource> subresources, final List<ApiLink> links) {
        final Set<String> seen = new HashSet<>();
        for (final ApiSubresource sub : subresources) {
            if (!seen.add(sub.parent().className() + "/" + sub.path())) {
                throw new IllegalStateException("the path '" + sub.path() + "' below " + sub.parent().className() + " is used twice");
            }
        }
        for (final ApiLink link : links) {
            if (!seen.add(link.parent().className() + "/" + link.path())) {
                throw new IllegalStateException("the path '" + link.path() + "' below " + link.parent().className() + " is used twice");
            }
        }
    }

    /** @return the sub-resources of all entities */
    public @NotNull @Unmodifiable List<ApiSubresource> getSubresources() {
        return subresources;
    }

    /** @return the links of all entities */
    public @NotNull @Unmodifiable List<ApiLink> getLinks() {
        return links;
    }

    /**
     * @param parent an entity
     * @param path   a path below it
     * @return the sub-resource there, or null if there is none
     */
    public @Nullable ApiSubresource findSubresource(final @NotNull ApiEntity parent, final @NotNull String path) {
        for (final ApiSubresource sub : subresources) {
            if (sub.parent().className().equals(parent.className()) && sub.path().equals(path)) {
                return sub;
            }
        }
        return null;
    }

    /**
     * @param parent an entity
     * @param path   a path below it
     * @return the link there, or null if there is none
     */
    public @Nullable ApiLink findLink(final @NotNull ApiEntity parent, final @NotNull String path) {
        for (final ApiLink link : links) {
            if (link.parent().className().equals(parent.className()) && link.path().equals(path)) {
                return link;
            }
        }
        return null;
    }

    /** @return the exposed entities, in the order of their registration */
    public @NotNull @Unmodifiable List<ApiEntity> getEntities() {
        return entities;
    }

    /**
     * @param path the path of an entity
     * @return the entity, or null if there is no exposed entity with that path
     */
    public @Nullable ApiEntity findByPath(final @NotNull String path) {
        for (final ApiEntity entity : entities) {
            if (entity.path().equals(path)) {
                return entity;
            }
        }
        return null;
    }

    private static ApiEntity buildEntity(final OrmDaoCollection<?> collection, final String className, final String path, final EntityDispatchTable<?, ?> table,
                                         final @Nullable RestOperations operations) {
        final String idColumn = table.getMetaData(JanitorOrm.MetaData.ID_FIELD);
        final String keyColumn = table.getMetaData(JanitorOrm.MetaData.KEY_FIELD);
        final String versionColumn = table.getMetaData(JanitorOrm.MetaData.VERSION_FIELD);
        final String modifiedColumn = table.getMetaData(JanitorOrm.MetaData.MODIFIED_FIELD);
        final List<ApiField> fields = new ArrayList<>();
        String idField = null;
        String keyField = null;
        String versionField = null;
        String modifiedField = null;
        for (final String name : table.streamAttributeNames().toList()) {
            final String column = table.getMetaData(name, JanitorOrm.MetaData.COLUMN_NAME);
            final ColumnTypeHint columnType = table.getMetaData(name, JanitorOrm.MetaData.COLUMN_TYPE);
            if (column == null || column.isBlank() || columnType == null) {
                continue; // not backed by a column: not exposed
            }
            if (Boolean.TRUE.equals(table.getMetaData(name, RestMetaData.HIDDEN))) {
                continue;
            }
            if (!OrmPropertyHandle.isAvailableIn(table.getMetaData(name, JanitorOrm.MetaData.VERSION_RANGE), collection.getSchemaVersion())) {
                continue;
            }
            final boolean isId = column.equals(idColumn);
            final boolean isVersion = column.equals(versionColumn);
            final boolean isModified = column.equals(modifiedColumn);
            if (isId) {
                idField = name;
            }
            if (column.equals(keyColumn)) {
                keyField = name;
            }
            if (isVersion) {
                versionField = name;
            }
            if (isModified) {
                modifiedField = name;
            }
            fields.add(buildField(collection, table, name, column, columnType, isId || isVersion || isModified));
        }
        if (idField == null) {
            throw new IllegalStateException("the id of " + className + " is not exposed, but entities cannot be addressed without it");
        }
        final Integer maxPageSize = table.getMetaData(RestMetaData.MAX_PAGE_SIZE);
        return new ApiEntity(className, path, table, List.copyOf(fields), idField, keyField, versionField, modifiedField,
                maxPageSize == null ? DEFAULT_MAX_PAGE_SIZE : maxPageSize, table.getMetaData(RestMetaData.DEFAULT_SORT),
                operations == null ? RestOperations.READ_ONLY.set() : operations.set());
    }

    private static ApiField buildField(final OrmDaoCollection<?> collection, final DispatchTable<?> table, final String name, final String column,
                                       final ColumnTypeHint columnType, final boolean readOnlyByRole) {
        final String reference = table.getMetaData(name, Janitor.MetaData.REF);
        boolean referenceByKey = false;
        if (reference != null) {
            final EntityDispatchTable<?, ?> target = collection.getEntityDispatchTable(reference);
            referenceByKey = target != null && target.getMetaData(JanitorOrm.MetaData.KEY_FIELD) != null;
        }
        final boolean hostNullable = !Boolean.FALSE.equals(table.getMetaData(name, Janitor.MetaData.HOST_NULLABLE));
        final boolean nullable = reference != null || hostNullable;
        final boolean required = Boolean.TRUE.equals(table.getMetaData(name, Janitor.MetaData.REQUIRED)) || !nullable;
        final boolean readOnly = readOnlyByRole || table instanceof GenericDispatchTable<?> generic && generic.isReadOnlyProperty(name);
        final boolean longText = columnType == ColumnTypeHint.CLOB || columnType == ColumnTypeHint.NCLOB;
        final boolean lazy = Boolean.TRUE.equals(table.getMetaData(name, JanitorOrm.MetaData.LAZY_LOAD));
        final boolean defaultAllowed = !longText && !lazy;
        final Boolean filterable = table.getMetaData(name, RestMetaData.FILTERABLE);
        final Boolean sortable = table.getMetaData(name, RestMetaData.SORTABLE);
        return new ApiField(name, column, columnType, nullable, required, readOnly,
                filterable == null ? defaultAllowed : filterable,
                sortable == null ? defaultAllowed : sortable,
                table.getMetaData(name, JanitorOrm.MetaData.MAX_LENGTH),
                table.getMetaData(name, JanitorOrm.MetaData.DECIMAL_PRECISION),
                table.getMetaData(name, JanitorOrm.MetaData.DECIMAL_SCALE),
                reference, referenceByKey,
                table.getMetaData(name, Janitor.MetaData.HELP),
                table.getMetaData(name, RestMetaData.EXAMPLE));
    }
}
