package com.eischet.janitor.orm.dao;


import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.StatementConfigurator;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.orm.FilterExpression;
import com.eischet.janitor.orm.entity.OrmEntity;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface Dao<T extends OrmEntity> extends JanitorObject {

    @NotNull Class<T> getEntityClass();

    @NotNull String getEntityClassName();

    @Nullable T findByKey(@NotNull DatabaseConnection conn,
                          @Nullable String key) throws DatabaseError;

    @Nullable T findById(@NotNull DatabaseConnection conn,
                         long id) throws DatabaseError;

    @NotNull
    @Unmodifiable
    List<T> findAll(@NotNull DatabaseConnection conn,
                    @Nullable Integer limit) throws DatabaseError;

    @NotNull
    @Unmodifiable
    default List<T> findAll(@NotNull DatabaseConnection conn) throws DatabaseError {
        return findAll(conn, null);
    }

    @NotNull
    @Unmodifiable
    List<T> findByQuery(@NotNull DatabaseConnection conn, @NotNull @Language("SQL") String query, @NotNull StatementConfigurator statementConfigurator) throws DatabaseError;


    @NotNull
    @Unmodifiable
    List<T> findByFilter(@NotNull DatabaseConnection conn,
                         @NotNull FilterExpression filterExpression,
                         @Nullable Integer limit) throws DatabaseError;

    @NotNull
    @Unmodifiable
    default List<T> findByFilter(@NotNull DatabaseConnection conn,
                                 @NotNull FilterExpression filterExpression) throws DatabaseError {
        return findByFilter(conn, filterExpression, null);
    }

    int countByFilter(@NotNull DatabaseConnection conn, @Nullable FilterExpression filterExpression) throws DatabaseError;

    default int countAll(@NotNull DatabaseConnection conn) throws DatabaseError {
        return countByFilter(conn, null);
    }

    void insert(@NotNull DatabaseConnection conn,
                @NotNull T record) throws DatabaseError;

    void update(@NotNull DatabaseConnection conn,
                @NotNull T record) throws DatabaseError;

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

    void setLogging(final DaoLogging logging);

}

