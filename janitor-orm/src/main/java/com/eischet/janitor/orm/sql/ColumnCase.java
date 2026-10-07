package com.eischet.janitor.orm.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * What is known about the letter case of the values in a text column. Clients set this as meta data on a
 * property (see {@link com.eischet.janitor.orm.JanitorOrm.MetaData#COLUMN_CASE}) when they can guarantee that the
 * column only ever holds ALL-UPPER or all-lower values, e.g. short codes.
 * <p>
 * {@code GenericDao} uses this to avoid wrapping the column in {@code lower(...)} for case-insensitive filters:
 * the search value is normalized in Java instead, which keeps the column "bare" so that indexes remain usable.
 * Getting this wrong (declaring UPPER for a column that contains mixed-case data) makes case-insensitive filters
 * miss rows, so only declare it for columns that are guaranteed to be uniform.
 * </p>
 */
public enum ColumnCase {

    /**
     * Anything goes; the default. Case-insensitive filters have to fold the column in the database.
     */
    MIXED,

    /**
     * The column only holds upper-case values.
     */
    UPPER,

    /**
     * The column only holds lower-case values.
     */
    LOWER;

    /**
     * @return true if values in such a column all share one case, so that search values can be normalized instead of the column
     */
    public boolean isUniform() {
        return this != MIXED;
    }

    /**
     * Converts a search value to the case that the column stores.
     */
    public @Nullable String normalize(final @Nullable String value) {
        if (value == null) {
            return null;
        }
        return switch (this) {
            case UPPER -> value.toUpperCase(Locale.ROOT);
            case LOWER -> value.toLowerCase(Locale.ROOT);
            case MIXED -> value;
        };
    }

    public static boolean isUniform(final @Nullable ColumnCase columnCase) {
        return columnCase != null && columnCase.isUniform();
    }

    @Override
    public @NotNull String toString() {
        return name();
    }
}
