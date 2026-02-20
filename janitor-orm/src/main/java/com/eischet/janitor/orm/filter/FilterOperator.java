package com.eischet.janitor.orm.filter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public enum FilterOperator {
    EQ("eq"),
    NEQ("neq"),
    LT("lt"),
    LTE("lte"),
    GT("gt"),
    GTE("gte"),
    STARTSWITH("startswith"),
    ENDSWITH("endswith"),
    CONTAINS("contains"),
    DOESNOTCONTAIN("doesnotcontain"),
    ISNULL("isnull"),
    ISNOTNULL("isnotnull"),
    ISEMPTY("isempty"),
    ISNOTEMPTY("isnotempty");

    private final String code;

    FilterOperator(final String code) {
        this.code = code;
    }

    public static final List<FilterOperator> OPERATORS = List.of(values());

    public static @NotNull FilterOperator fromCode(final String code) {
        return OPERATORS.stream()
                .filter(it -> Objects.equals(it.code, code))
                .findFirst()
                .orElse(FilterOperator.EQ);
    }

    public String getCode() {
        return code;
    }
}
