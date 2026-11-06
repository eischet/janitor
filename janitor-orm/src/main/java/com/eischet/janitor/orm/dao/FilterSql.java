// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.filter.MalformedExpression;
import com.eischet.janitor.orm.sql.ColumnCase;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The parts of turning a {@link FilterExpression} into SQL that entity DAOs ({@link GenericDao}) and join DAOs ({@link JoinDao}) share:
 * the condition for one column, the special handling of references to nothing, and the sub-query for a path through a reference.
 */
final class FilterSql {

    /**
     * Matches anything that is not a field, or a path through references like "product.shortCode".
     */
    static final Predicate<String> INVALID_FIELD = Pattern.compile("[a-zA-Z0-9_@-]+(\\.[a-zA-Z0-9_@-]+)*").asMatchPredicate().negate();

    private FilterSql() {
    }

    /**
     * Creates the prepper that binds the value of a filter expression, according to the type of the value and, for booleans,
     * of the column, see {@link GenericDao#getPrepper(FilterExpression, ColumnTypeHint)}.
     */
    static @NotNull Prepper prepperFor(final @NotNull FilterExpression filterExpression, final @Nullable ColumnTypeHint columnTypeHint) {
        if (filterExpression.isDate()) {
            return GenericDao.PREP_DATE.getPrepper(filterExpression);
        } else if (filterExpression.isDateTime()) {
            return GenericDao.PREP_DATETIME.getPrepper(filterExpression);
        } else if (filterExpression.isDouble()) {
            return GenericDao.PREP_DOUBLE.getPrepper(filterExpression);
        } else if (filterExpression.isBoolean()) {
            return columnTypeHint == ColumnTypeHint.BOOL_CHAR ? GenericDao.PREP_BOOL_CHAR.getPrepper(filterExpression) : GenericDao.PREP_BOOLEAN.getPrepper(filterExpression);
        } else if (filterExpression.isLong()) {
            return GenericDao.PREP_LONG.getPrepper(filterExpression);
        } else {
            return GenericDao.PREP_STRING.getPrepper(filterExpression);
        }
    }

    /**
     * The condition for "is null" and "is not null" on a reference column whose reference to nothing can also be stored as an id,
     * see {@link com.eischet.janitor.orm.JanitorOrm.MetaData#NULL_REFERENCE_ID}.
     *
     * @return the condition, or null if the column or the operator needs no special handling
     */
    static @Nullable String nullReferenceCondition(final @NotNull FilterOperator operator, final @NotNull String quotedColumn, final @Nullable Long nullReferenceId) {
        if (nullReferenceId == null) {
            return null;
        }
        return switch (operator) {
            case ISNULL -> "(" + quotedColumn + " is null or " + quotedColumn + " = " + nullReferenceId + ")";
            case ISNOTNULL -> "(" + quotedColumn + " is not null and " + quotedColumn + " <> " + nullReferenceId + ")";
            default -> null;
        };
    }

    /**
     * The condition of an expression for one column, see {@link GenericDao#applyExpressionToColumn(FilterExpression, String, ColumnTypeHint, ColumnCase, Consumer)}.
     *
     * @param dialect         the dialect of the database
     * @param original        the expression
     * @param quotedColumn    the column, already quoted for the database
     * @param columnTypeHint  the type of the column, or null if unknown
     * @param columnCase      the letter case of the column's values, or null if unknown
     * @param prepperFor      creates the prepper that binds the value of the expression
     * @param prepperConsumer receives the preppers, in the order of the parameters in the SQL
     * @return the SQL condition
     */
    static String applyToColumn(final @NotNull DatabaseDialect dialect,
                                final @NotNull FilterExpression original,
                                final @NotNull String quotedColumn,
                                final @Nullable ColumnTypeHint columnTypeHint,
                                final @Nullable ColumnCase columnCase,
                                final @NotNull BiFunction<FilterExpression, ColumnTypeHint, Prepper> prepperFor,
                                final @NotNull Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        @Nullable final FilterOperator op = original.getOperator();
        final boolean textMatch = switch (op) {
            case STARTSWITH, ENDSWITH, CONTAINS, DOESNOTCONTAIN -> true;
            default -> false;
        };
        final boolean ignoreCase;
        if (textMatch) {
            ignoreCase = !Boolean.FALSE.equals(original.getIgnoreCase()); // text searches ignore case unless asked otherwise
        } else {
            ignoreCase = original.isString() && Boolean.TRUE.equals(original.getIgnoreCase()); // comparisons only on request
        }
        // For uniformly cased columns, adjust the value to the column and leave the column itself alone.
        final boolean normalizeValue = ignoreCase && original.isString() && ColumnCase.isUniform(columnCase);
        final boolean foldColumn = ignoreCase && !normalizeValue;
        final FilterExpression filterExpression;
        if (normalizeValue) {
            filterExpression = original.deepCopy();
            filterExpression.setValueString(columnCase.normalize(original.getValueString()));
        } else {
            filterExpression = original;
        }
        final Prepper simpleEquality = prepperFor.apply(filterExpression, columnTypeHint);
        final String left = foldColumn ? dialect.foldCase(quotedColumn) : quotedColumn;
        final String right = foldColumn ? dialect.foldCase("?") : "?";
        return switch (op) {
            case EQ -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " = " + right;
            }
            case NEQ -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " != " + right;
            }
            case LT -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " < " + right;
            }
            case LTE -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " <= " + right;
            }
            case GT -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " > " + right;
            }
            case GTE -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " >= " + right;
            }
            case STARTSWITH -> {
                prepperConsumer.accept(GenericDao.PREP_STARTS_WITH_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, false, foldColumn);
            }
            case ENDSWITH -> {
                prepperConsumer.accept(GenericDao.PREP_ENDS_WITH_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, false, foldColumn);
            }
            case CONTAINS -> {
                prepperConsumer.accept(GenericDao.PREP_CONTAINS_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, false, foldColumn);
            }
            case DOESNOTCONTAIN -> {
                prepperConsumer.accept(GenericDao.PREP_CONTAINS_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, true, foldColumn);
            }
            case ISNULL -> quotedColumn + " is null";
            case ISNOTNULL -> quotedColumn + " is not null";
            case ISEMPTY -> dialect.isEmptyCondition(quotedColumn);
            case ISNOTEMPTY -> dialect.isNotEmptyCondition(quotedColumn);
        };
    }

    /**
     * Finds the DAO of the entities that a field refers to.
     *
     * @param collection       the collection of the DAO that the field belongs to
     * @param columnForField   the columns of that DAO's fields
     * @param mappingForColumn the mappings of that DAO's columns
     * @param field            the field, which holds a reference
     * @param path             the whole path, for error messages
     * @return the DAO
     * @throws MalformedExpression if the field does not exist or does not hold a reference to an entity of a GenericDao
     */
    static @NotNull GenericDao<?, ?> referencedDao(final @NotNull OrmDaoCollection<?> collection,
                                                   final @NotNull Map<String, String> columnForField,
                                                   final @NotNull Map<String, ? extends ColumnMapping<?>> mappingForColumn,
                                                   final @NotNull String field,
                                                   final @NotNull String path) throws MalformedExpression {
        final String column = columnForField.get(field);
        if (column == null) {
            throw new MalformedExpression("missing column for field '" + field + "' of path '" + path + "'");
        }
        final @Nullable ColumnMapping<?> mapping = mappingForColumn.get(column);
        final @Nullable String referenceType = mapping == null ? null : mapping.referenceType();
        if (referenceType == null) {
            throw new MalformedExpression("field '" + field + "' does not refer to another entity, so the path '" + path + "' is invalid");
        }
        if (!(collection.getDao(referenceType) instanceof GenericDao<?, ?> referencedDao)) {
            throw new MalformedExpression("cannot filter by '" + path + "', because there is no DAO for " + referenceType + " that supports filters");
        }
        return referencedDao;
    }

    /**
     * Converts an expression on a path like "product.shortCode" to a sub-query on the referenced entity's table:
     * {@code product_id in (select product_id from product where short_code = ?)}. The referenced DAO builds the inner condition,
     * so its own column types, letter cases and custom expression handlers apply, and longer paths are resolved one step per DAO.
     * Following the rules of SQL, records without a referenced entity never match, not even for "is null".
     *
     * @param dialect          the dialect of the database
     * @param referenceColumn  the column that holds the reference, not quoted
     * @param referencedDao    the DAO of the referenced entities
     * @param filterExpression an expression whose field is a path
     * @param prepperConsumer  receives the preppers, in the order of the parameters in the SQL
     * @return the SQL condition
     */
    static String pathToSql(final @NotNull DatabaseDialect dialect,
                            final @NotNull String referenceColumn,
                            final @NotNull GenericDao<?, ?> referencedDao,
                            final @NotNull FilterExpression filterExpression,
                            final @NotNull Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        final String path = filterExpression.getField();
        final FilterExpression inner = filterExpression.deepCopy();
        inner.setField(path.substring(path.indexOf('.') + 1));
        return dialect.quoteColumn(referenceColumn)
               + " in (select " + dialect.quoteColumn(referencedDao.idColumn) + " from " + referencedDao.tableName
               + " where " + referencedDao.expressionToSql(inner, prepperConsumer) + ")";
    }

}
