// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.brrr;

import com.eischet.janitor.api.types.StringMappedEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/** How strongly a Brrr notification interrupts the user. */
public enum BrrrInterruptionLevel implements StringMappedEnum {
    PASSIVE("passive"), ACTIVE("active"), TIME_SENSITIVE("time-sensitive");

    private final String stringRepresentation;

    BrrrInterruptionLevel(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    @Override
    public @NotNull String getStringRepresentation() {
        return stringRepresentation;
    }

    public static @NotNull @Unmodifiable List<BrrrInterruptionLevel> VALUES = List.of(values());

    /**
     * @param stringRepresentation the string, e.g. "passive"
     * @return the matching level, or null if the string is blank or unknown
     */
    public static @Nullable BrrrInterruptionLevel fromString(final @Nullable String stringRepresentation) {
        if (stringRepresentation == null || stringRepresentation.isBlank()) {
            return null;
        }
        return VALUES.stream().filter(sound -> sound.getStringRepresentation().equals(stringRepresentation)).findFirst().orElse(null);
    }
}
