package com.eischet.janitor.orm.sql;

public enum ColumnTypeHint {
    INT,
    VARCHAR,
    NVARCHAR,
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
