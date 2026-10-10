package com.eischet.janitor.api.types;

import org.jetbrains.annotations.NotNull;

/** Implemented by enums that have a canonical string representation, which is used e.g. when converting them to and from script values. */
public interface StringMappedEnum {

    @NotNull String getStringRepresentation();

}
