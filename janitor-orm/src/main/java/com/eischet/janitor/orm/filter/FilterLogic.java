package com.eischet.janitor.orm.filter;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public enum FilterLogic {
    AND("and"),
    OR("or");

    private final String code;

    FilterLogic(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static final List<FilterLogic> LOGIC = List.of(values());

    public static @Nullable FilterLogic fromCode(final String code) {
        return LOGIC.stream()
                .filter(it -> Objects.equals(it.code, code))
                .findFirst()
                .orElse(null);
    }

}
