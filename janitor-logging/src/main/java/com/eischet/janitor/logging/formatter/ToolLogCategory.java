/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor.logging.formatter;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

/** The category of a log record: info, debug, error or warning. Each category has a one-letter code, e.g. for storing it. */
public enum ToolLogCategory {
    INFO("i", "I"),
    DEBUG("d", "D"),
    ERROR("e", "E"),
    WARNING("w", "W"),
    INVALID("?", "?")
    ;

    private final @NotNull String code;
    private final @NotNull String compactRepresentation;

    ToolLogCategory(final @NotNull String code, final @NotNull String compactRepresentation) {
        this.code = code;
        this.compactRepresentation = compactRepresentation;
    }

    /**
     * @return the one-letter representation for console output
     */
    public @NotNull String getCompactRepresentation() {
        return compactRepresentation;
    }

    /**
     * @param level the java.util.logging level, may be null
     * @return the matching category
     */
    public static @NotNull ToolLogCategory forLevel(final @Nullable Level level) {
        if (level != null) {
            if (level.intValue() >= Level.SEVERE.intValue()) {
                return ERROR;
            }
            if (level.intValue() >= Level.WARNING.intValue()) {
                return WARNING;
            }
            if (level.intValue() >= Level.INFO.intValue()) {
                return INFO;
            }
        }
        return DEBUG;
    }

    /**
     * @return the one-letter code of this category
     */
    public @NotNull String getCode() {
        return code;
    }

    /**
     * @param code the one-letter code
     * @return the category, or INVALID if the code is unknown
     */
    public static @NotNull ToolLogCategory forCode(final @Nullable String code) {
        return Arrays.stream(values()).filter(lc -> Objects.equals(lc.code, code)).findFirst().orElse(INVALID);
    }

}
