// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

/** The operators of a {@link ComparisonNode}. */
public enum ComparisonOperator {
    /** {@code ==} */
    EQUALS,
    /** {@code !=} or {@code <>} */
    NOT_EQUALS,
    /** {@code ~=}, equality ignoring case */
    EQUALS_IGNORE_CASE,
    /** {@code <} */
    LESS_THAN,
    /** {@code <=} */
    LESS_THAN_OR_EQUALS,
    /** {@code >} */
    GREATER_THAN,
    /** {@code >=} */
    GREATER_THAN_OR_EQUALS,
    /** {@code ~}, matches a glob pattern or regex */
    MATCHES,
    /** {@code !~}, does not match a glob pattern or regex */
    MATCHES_NOT,
    /** {@code in} */
    IN,
    /** {@code not in} */
    NOT_IN
}
