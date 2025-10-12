/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor.logging.formatter;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

public enum ToolLogCategory {
    INFO("i", "I"),
    DEBUG("d", "D"),
    ERROR("e", "E"),
    WARNING("w", "W"),
    INVALID("?", "?")
    ;

    // log_category char(1) not null, -- i=info, d=debug, e=error, w=warning

    private final @NotNull String code;
    private final @NotNull String compactRepresentation;

    ToolLogCategory(final @NotNull String code, final @NotNull String compactRepresentation) {
        this.code = code;
        this.compactRepresentation = compactRepresentation;
    }

    public @NotNull String getCompactRepresentation() {
        return compactRepresentation;
    }

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

    public @NotNull String getCode() {
        return code;
    }

    public static @NotNull ToolLogCategory forCode(final @Nullable String code) {
        return Arrays.stream(values()).filter(lc -> Objects.equals(lc.code, code)).findFirst().orElse(INVALID);
    }

}
