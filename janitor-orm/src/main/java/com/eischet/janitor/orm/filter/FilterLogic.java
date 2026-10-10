package com.eischet.janitor.orm.filter;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** The logic by which the filters of a group are combined: all must match (AND), or at least one must match (OR). */
public enum FilterLogic {
    AND("and"),
    OR("or");

    private final String code;

    FilterLogic(String code) {
        this.code = code;
    }

    /**
     * @return the code of this logic, as used in JSON
     */
    public String getCode() {
        return code;
    }

    public static final List<FilterLogic> LOGIC = List.of(values());

    /**
     * @param code the code, "and" or "or"
     * @return the logic, or null if the code is unknown
     */
    public static @Nullable FilterLogic fromCode(final String code) {
        return LOGIC.stream()
                .filter(it -> Objects.equals(it.code, code))
                .findFirst()
                .orElse(null);
    }

}
