// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.sql;

/** The type of a database column, as far as it matters for reading and writing values. */
public enum ColumnTypeHint {
    INT,
    VARCHAR,
    NVARCHAR,
    /**
     * A long text in a regular (database character set) CLOB column.
     */
    CLOB,
    /**
     * A long text in a national character set NCLOB column.
     */
    NCLOB,
    BIT,
    /**
     * A boolean stored as a single-character string, {@code "y"} or {@code "n"}, rather than as a native
     * boolean/numeric column. Use this for schemas that predate a real boolean column type (e.g. Oracle-style
     * {@code CHAR(1) check (... in ('y', 'n'))} flags) instead of {@link #BIT}, which assumes integer 0/1 storage.
     */
    BOOL_CHAR,
    DATETIME,
    DATE,
    DECIMAL
}
