// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import org.jetbrains.annotations.NotNull;

/**
 * Turns a condition on a pseudo field into a condition on real fields.
 * <p>
 * A pseudo field is a name that filters may use although the entity has no column for it, e.g. "open" for {@code status eq "o"},
 * or "createdDate@date" for the date part of the date-time field "createdDate". A DAO rewrites such conditions before it builds
 * the SQL, so every kind of filter accepts them: search masks, filter maps, lambdas, and paths through references like
 * "ticket.open". Register a rewrite on a property with {@link com.eischet.janitor.orm.JanitorOrm.MetaData#FILTER_REWRITE}, or
 * as a field of its own with {@link FilterFields}.
 * </p>
 */
@FunctionalInterface
public interface FilterRewrite {

    /**
     * Rewrites a condition on the pseudo field.
     * @param condition the condition, whose field is the pseudo field
     * @return a new condition on real fields, which may again use pseudo fields
     * @throws MalformedExpression if the condition cannot be used with the pseudo field
     */
    @NotNull FilterExpression rewrite(@NotNull FilterExpression condition) throws MalformedExpression;

    /**
     * A boolean shorthand: "open eq true" means the filter, "open eq false" its opposite.
     * @param meaning the filter that the shorthand stands for
     * @return the rewrite
     */
    static @NotNull FilterRewrite flag(final @NotNull FilterExpression meaning) {
        return condition -> condition.testsForTrue() ? meaning.deepCopy() : meaning.negated();
    }

    /**
     * Another name for a field or a path, e.g. "section" for "department.section".
     * @param path the field or path
     * @return the rewrite
     */
    static @NotNull FilterRewrite alias(final @NotNull String path) {
        return condition -> condition.withField(path);
    }

    /**
     * Compares only the date of a date-time field: "createdDate@date eq 2026-11-09" finds the whole day.
     * @param dateTimeField the date-time field
     * @return the rewrite
     */
    static @NotNull FilterRewrite dateOf(final @NotNull String dateTimeField) {
        return condition -> DateRange.of(dateTimeField, condition);
    }

}
