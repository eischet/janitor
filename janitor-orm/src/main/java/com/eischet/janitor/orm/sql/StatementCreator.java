// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.sql;


import com.eischet.dbxs.dialects.DatabaseDialect;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.stream.Collectors;

/** Creates SQL statements for the basic operations on a table, using the quoting rules of a {@link DatabaseDialect}. */
public class StatementCreator {

    protected final DatabaseDialect dialect;

    public StatementCreator(final DatabaseDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * Quotes column names for the database.
     * @param fields the column names
     * @return the quoted column names
     */
    protected @NotNull @Unmodifiable List<String> quoteAllFields(final @NotNull List<String> fields) {
        return fields.stream().map(dialect::quoteColumn).toList();
    }

    /**
     * Creates a statement that selects the given columns of all rows of a table.
     * @param table the name of the table
     * @param fields the columns to select
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name is blank, or there are no fields
     */
    @NotNull
    @Language("sql")
    public String createSelectAllStatement(final @NotNull String table,
                                           final @NotNull @Unmodifiable List<String> fields) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("the list of fields must not be empty for table '" + table + "'");
        }
        //noinspection LanguageMismatch
        return "select " + String.join(", ", quoteAllFields(fields)) + " from " + table;
    }

    /**
     * Creates a statement that counts all rows of a table.
     * @param table the name of the table
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name is blank
     */
    @NotNull
    @Language("sql")
    public String createCountStatement(final @NotNull String table) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        //noinspection LanguageMismatch
        return "select count(*) from " + table;
    }



    /**
     * Creates a statement that selects the given columns of the rows where a column has a given value, which is a parameter.
     * @param table the name of the table
     * @param fields the columns to select
     * @param whereField the column to compare with the parameter
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name or the where field is blank, or there are no fields
     */
    @NotNull
    @Language("sql")
    public String createSelectStatement(final @NotNull String table,
                                        final @NotNull @Unmodifiable List<String> fields,
                                        final @NotNull String whereField) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("the list of fields must not be empty for table '" + table + "'");
        }
        if (whereField.isBlank()) {
            throw new IllegalArgumentException("the 'where' field must not be blank");
        }
        //noinspection LanguageMismatch
        return "select " + String.join(", ", quoteAllFields(fields)) + " from " + table + " where " + dialect.quoteColumn(whereField) + " = ?";
    }

    /**
     * Create an insert statement, with "?" placeholders, from the table name and the list of fields.
     *
     * @param table  table name
     * @param columns list of columns
     * @return a valid SQL statement
     * @throws IllegalArgumentException when the list of fields is empty or the table name is blank
     */
    @NotNull
    @Language("sql")
    public String createInsertStatement(final @NotNull String table,
                                        final @NotNull @Unmodifiable List<String> columns) throws IllegalArgumentException {

        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("the list of columns must not be empty");
        }

        final StringBuilder out = new StringBuilder();
        out.append("insert into ").append(table).append(" (");
        out.append(String.join(", ", quoteAllFields(columns)));
        out.append(") values (?");
        for (int i = 1, max = columns.size(); i < max; i++) {
            out.append(", ?");
        }
        out.append(")");
        return out.toString();
    }

    /**
     * Creates a statement that deletes the rows where a column has a given value, which is a parameter.
     * @param table the name of the table
     * @param whereColumn the column to compare with the parameter
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name or the where column is blank
     */
    @NotNull
    @Language("sql")
    public String createDeleteStatement(final @NotNull String table, final @NotNull String whereColumn) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (whereColumn.isBlank()) {
            throw new IllegalArgumentException("the 'where' column must not be blank");
        }
        //noinspection LanguageMismatch
        return "delete from " + table + " where " + dialect.quoteColumn(whereColumn) + " = ?";
    }

    /**
     * Creates a statement that deletes the rows where all of the given columns have a given value, each of which is a parameter.
     * @param table the name of the table
     * @param whereColumns the columns to compare with the parameters
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name is blank, or there are no where columns
     */
    @NotNull
    @Language("sql")
    public String createDeleteStatement(final @NotNull String table, final @NotNull List<String> whereColumns) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (whereColumns.isEmpty()) {
            throw new IllegalArgumentException("the list of 'where' columns must not be empty");
        }
        //noinspection LanguageMismatch
        return "delete from " + table + " where " + whereColumns.stream().map(dialect::quoteColumn).map(it -> it + " = ?").collect(Collectors.joining(" and "));
    }


    /**
     * Creates a statement that updates the given columns of the rows where a column has a given value. The parameters are the new values, followed by the value to compare.
     * @param table the name of the table
     * @param columns the columns to update
     * @param whereColumn the column to compare with the last parameter
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name or the where column is blank, or there are no columns
     */
    @NotNull
    @Language("sql")
    public String createUpdateStatement(final @NotNull String table,
                                        final @NotNull @Unmodifiable List<String> columns,
                                        final @NotNull String whereColumn) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("the list of columns must not be empty");
        }
        if (whereColumn.isBlank()) {
            throw new IllegalArgumentException("the 'where' column must not be blank");
        }
        final StringBuilder out = new StringBuilder();
        out.append("update ").append(table).append(" set ");
        out.append(columns.stream().map(col -> String.format("%s = ?", dialect.quoteColumn(col))).collect(Collectors.joining(", ")));
        out.append(" where ").append(dialect.quoteColumn(whereColumn)).append(" = ?");
        return out.toString();
    }

    /**
     * Creates a statement that updates the given columns of the rows where all of the given columns have a given value. The parameters are the new values, followed by the values to compare.
     * @param table the name of the table
     * @param columns the columns to update
     * @param whereColumns the columns to compare with the last parameters
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name is blank, or there are no columns or where columns
     */
    @NotNull
    @Language("sql")
    public String createUpdateStatement(final @NotNull String table,
                                        final @NotNull @Unmodifiable List<String> columns,
                                        final @NotNull @Unmodifiable List<String> whereColumns) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("the list of columns must not be empty");
        }
        if (whereColumns.isEmpty()) {
            throw new IllegalArgumentException("the 'where' columns must not be empty");
        }
        final StringBuilder out = new StringBuilder();
        out.append("update ").append(dialect.quoteTableName(table)).append(" set ");
        out.append(columns.stream().map(col -> String.format("%s = ?", dialect.quoteColumn(col))).collect(Collectors.joining(", ")));
        out.append(" where ").append(whereColumns.stream().map(dialect::quoteColumn).map(it -> it + " = ?").collect(Collectors.joining(" and ")));
        return out.toString();
    }

    /**
     * Creates a statement that counts the rows where all of the given columns have a given value, each of which is a parameter.
     * @param table the name of the table
     * @param whereColumns the columns to compare with the parameters
     * @return the SQL statement
     * @throws IllegalArgumentException if the table name is blank, or there are no where columns
     */
    @NotNull
    @Language("sql")
    public String createCountStatement(final @NotNull String table,
                                       final @NotNull @Unmodifiable List<String> whereColumns) throws IllegalArgumentException {
        if (table.isBlank()) {
            throw new IllegalArgumentException("the table name must not be blank");
        }
        if (whereColumns.isEmpty()) {
            throw new IllegalArgumentException("the 'where' columns must not be empty");
        }
        final StringBuilder out = new StringBuilder();
        out.append("select count (*) from ").append(dialect.quoteTableName(table));
        out.append(" where ").append(whereColumns.stream().map(dialect::quoteColumn).map(it -> it + " = ?").collect(Collectors.joining(" and ")));
        return out.toString();

    }

}
