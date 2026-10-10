// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;


import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.StatementConfigurator;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKeySearchResult;
import com.eischet.janitor.toolbox.listeners.ListenerRegistration;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A data access object: it finds, inserts, updates and deletes the entities of one type in a database table.
 * DAOs are also available to scripts.
 * @param <T> the type of the entities
 */
public interface Dao<T extends OrmEntity> extends JanitorObject {

    @NotNull Class<T> getEntityClass();

    @NotNull String getEntityClassName();

    @Nullable T findByKey(@NotNull DatabaseConnection conn,
                          @Nullable String key) throws DatabaseError;

    @Nullable T findById(@NotNull DatabaseConnection conn,
                         long id) throws DatabaseError;

    /**
     * Finds all entities.
     * @param conn the database connection
     * @param limit the maximum number of entities, or null for no limit
     * @return the entities
     * @throws DatabaseError on database errors
     */
    @NotNull
    @Unmodifiable
    List<T> findAll(@NotNull DatabaseConnection conn,
                    @Nullable Integer limit) throws DatabaseError;

    /**
     * Finds all entities.
     * @param conn the database connection
     * @return the entities
     * @throws DatabaseError on database errors
     */
    @NotNull
    @Unmodifiable
    default List<T> findAll(@NotNull DatabaseConnection conn) throws DatabaseError {
        return findAll(conn, null);
    }

    /**
     * Finds entities with a custom query.
     * @param conn the database connection
     * @param query the SQL text; it must select all the columns of the entity
     * @param statementConfigurator sets the parameters of the query
     * @return the entities
     * @throws DatabaseError on database errors
     */
    @NotNull
    @Unmodifiable
    List<T> findByQuery(@NotNull DatabaseConnection conn, @NotNull @Language("SQL") String query, @NotNull StatementConfigurator statementConfigurator) throws DatabaseError;

    @NotNull @Unmodifiable List<T> findByFilter(@NotNull DatabaseConnection conn, @NotNull FilterQuery filterQuery) throws DatabaseError;

    /**
     * Counts the entities that match a filter.
     * @param conn the database connection
     * @param filterExpression the filter, or null to count all entities
     * @return the number of entities
     * @throws DatabaseError on database errors
     */
    int countByFilter(@NotNull DatabaseConnection conn, @Nullable FilterExpression filterExpression) throws DatabaseError;

    /**
     * Counts all entities.
     * @param conn the database connection
     * @return the number of entities
     * @throws DatabaseError on database errors
     */
    default int countAll(@NotNull DatabaseConnection conn) throws DatabaseError {
        return countByFilter(conn, null);
    }

    /**
     * Inserts an entity.
     * @param conn the database connection
     * @param record the entity
     * @throws DatabaseError on database errors
     */
    void insert(@NotNull DatabaseConnection conn,
                @NotNull T record) throws DatabaseError;

    /**
     * Updates an entity.
     * @param conn the database connection
     * @param record the entity
     * @throws DatabaseError on database errors
     */
    void update(@NotNull DatabaseConnection conn,
                @NotNull T record) throws DatabaseError;

    /**
     * Deletes an entity.
     * @param conn the database connection
     * @param record the entity
     * @throws DatabaseError on database errors
     */
    void delete(@NotNull DatabaseConnection conn,
                @NotNull T record) throws DatabaseError;

    /**
     * Returns all records where the given column has the given value.
     *
     * @param conn the database connection
     * @param foreignKeyColumn the column name to search for
     * @param foreignKeyValue the value to search for
     * @return the list of records
     * @throws DatabaseError if there is an error while executing the query
     */
    @NotNull
    @Unmodifiable
    List<T> findByAssociation(final @NotNull DatabaseConnection conn, final String foreignKeyColumn, final long foreignKeyValue) throws DatabaseError;

    /**
     * Like findByAssociation, but automatically creates a database transaction, so this can be called more easily.
     * @param foreignKeyColumn the column name to search for
     * @param parentEntity the parent entity to search for
     * @return the list of records
     */
    @NotNull
    @Unmodifiable
    List<T> lazyLoadByAssociation(final String foreignKeyColumn, final OrmEntity parentEntity);


    @Nullable T lazyLoadById(long id);
    @Nullable T lazyLoadByKey(String key);

    // TODO: should lazyLoadByAssociation better throw an exception on errors?

    /**
     * Sets the logging for lazy loading.
     * @param logging the logging, or null for none
     */
    void setLogging(final DaoLogging logging);

    /**
     * Loads the entity that a search result refers to.
     * @param searchResult the search result
     * @return the entity, or null if it does not exist
     */
    default T findBySearchResult(ForeignKeySearchResult<T> searchResult) {
        return lazyLoadById(searchResult.getId());
    }

    /**
     * Creates a search result for an entity.
     * @param entity the entity
     * @return the search result
     */
    default ForeignKeySearchResult<T> toSearchResult(T entity) {
        return new ForeignKeySearchResult<>(this, entity.getId(), entity.getKey(), entity.getName(), entity.isSoftDeleted());
    }

    /**
     * Adds a listener that is notified about inserts, updates and deletes.
     * @param listener the listener
     * @return a registration that can be used to remove the listener
     */
    ListenerRegistration addChangeListener(EntityChangeListener<T> listener);

    /**
     * @return the dispatch table of the entities
     */
    DispatchTable<T> getEntityDispatchTable();
}

