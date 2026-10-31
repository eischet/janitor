// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Turns a {@link FilterExpression} into the Janitor code of a lambda, e.g. {@code item -> item.shortCode == "FOO"}, which
 * {@link LambdaFilter} turns back into an equivalent filter expression. So a filter that was built in a UI can be shown
 * and edited as code, and code can be loaded into the UI.
 * <p>
 * Equivalent means: the same query. The code uses the canonical form of each condition, e.g. {@code startsWithIgnoreCase}
 * for a STARTSWITH without {@code ignoreCase}, which ignores case by default; {@link #normalize} shows the expression that
 * the code stands for. Incomplete conditions are left out, like {@link FilterExpression#compress()} does. Comparisons
 * other than equality that ignore case have no form in code; they are written as exact comparisons.
 * </p>
 */
public final class FilterExpressionCode {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");

    private FilterExpressionCode() {
    }

    /**
     * Writes a filter expression as a lambda.
     * @param expression the filter expression
     * @param parameter the name of the lambda's parameter, e.g. "item"
     * @return the code, e.g. {@code item -> item.shortCode == "FOO"}, or null if the expression has no complete conditions
     */
    public static @Nullable String toLambda(final @NotNull FilterExpression expression, final @NotNull String parameter) {
        final String condition = toCondition(expression, parameter);
        return condition == null ? null : parameter + " -> " + condition;
    }

    /**
     * Writes a filter expression as a condition on a variable.
     * @param expression the filter expression
     * @param parameter the name of the variable that holds the entity, e.g. "item"
     * @return the code, e.g. {@code item.shortCode == "FOO"}, or null if the expression has no complete conditions
     */
    public static @Nullable String toCondition(final @NotNull FilterExpression expression, final @NotNull String parameter) {
        if (expression.isGroup()) {
            final List<String> parts = expression.getFilters().stream()
                    .map(part -> toCondition(part, parameter))
                    .filter(Objects::nonNull)
                    .toList();
            if (parts.isEmpty()) {
                return null;
            }
            if (parts.size() == 1) {
                return parts.getFirst();
            }
            final String logic = expression.getLogic() == FilterLogic.OR ? " or " : " and ";
            return parts.stream().map(part -> "(" + part + ")").collect(Collectors.joining(logic));
        }
        if (expression.isIncomplete()) {
            return null;
        }
        final String field = parameter + "." + expression.getField();
        final boolean exactText = Boolean.FALSE.equals(expression.getIgnoreCase()); // text searches ignore case unless set to false
        final boolean ignoreCaseEquality = expression.isString() && Boolean.TRUE.equals(expression.getIgnoreCase()); // comparisons only if set to true
        return switch (Objects.requireNonNull(expression.getOperator())) {
            case EQ -> ignoreCaseEquality ? field + " ~= " + value(expression) : field + " == " + value(expression);
            case NEQ -> ignoreCaseEquality ? "not (" + field + " ~= " + value(expression) + ")" : field + " != " + value(expression);
            case LT -> field + " < " + value(expression);
            case LTE -> field + " <= " + value(expression);
            case GT -> field + " > " + value(expression);
            case GTE -> field + " >= " + value(expression);
            case STARTSWITH -> field + (exactText ? ".startsWith(" : ".startsWithIgnoreCase(") + value(expression) + ")";
            case ENDSWITH -> field + (exactText ? ".endsWith(" : ".endsWithIgnoreCase(") + value(expression) + ")";
            case CONTAINS -> field + (exactText ? ".contains(" : ".containsIgnoreCase(") + value(expression) + ")";
            case DOESNOTCONTAIN -> "not " + field + (exactText ? ".contains(" : ".containsIgnoreCase(") + value(expression) + ")";
            case ISNULL -> field + " == null";
            case ISNOTNULL -> field + " != null";
            case ISEMPTY -> field + " == null or " + field + " == \"\"";
            case ISNOTEMPTY -> field + " != null and " + field + " != \"\"";
        };
    }

    /**
     * Returns the expression that {@link #toLambda} writes code for, in the form {@link LambdaFilter} reads it back: incomplete
     * conditions left out, groups with a single condition replaced by it, nested groups of the same logic flattened, and
     * {@code ignoreCase} spelled out where it makes a difference. For tests, and to compare expressions by their meaning.
     * @param expression the filter expression
     * @return the normalized expression, or null if it has no complete conditions
     */
    public static @Nullable FilterExpression normalize(final @NotNull FilterExpression expression) {
        if (expression.isGroup()) {
            final List<FilterExpression> parts = new ArrayList<>();
            for (final FilterExpression part : expression.getFilters()) {
                final FilterExpression normalized = normalize(part);
                if (normalized == null) {
                    continue;
                }
                if (normalized.isGroup() && normalized.getLogic() == expression.getLogic()) {
                    parts.addAll(normalized.getFilters());
                } else {
                    parts.add(normalized);
                }
            }
            if (parts.isEmpty()) {
                return null;
            }
            if (parts.size() == 1) {
                return parts.getFirst();
            }
            return expression.getLogic() == FilterLogic.OR ? FilterExpression.or(parts) : FilterExpression.and(parts);
        }
        if (expression.isIncomplete()) {
            return null;
        }
        final FilterExpression result = expression.deepCopy();
        switch (Objects.requireNonNull(expression.getOperator())) {
            case STARTSWITH, ENDSWITH, CONTAINS, DOESNOTCONTAIN -> result.setIgnoreCase(!Boolean.FALSE.equals(expression.getIgnoreCase()));
            case EQ, NEQ -> result.setIgnoreCase(expression.isString() && Boolean.TRUE.equals(expression.getIgnoreCase()) ? Boolean.TRUE : null);
            default -> result.setIgnoreCase(null);
        }
        return result;
    }

    private static @NotNull String value(final @NotNull FilterExpression expression) {
        if (expression.getValueString() != null) {
            return string(expression.getValueString());
        }
        if (expression.getValueLong() != null) {
            return Long.toString(expression.getValueLong());
        }
        if (expression.getValueDouble() != null) {
            return number(expression.getValueDouble());
        }
        if (expression.getValueBoolean() != null) {
            return expression.getValueBoolean() ? "true" : "false";
        }
        if (expression.getValueDate() != null) {
            return "@" + DATE.format(expression.getValueDate());
        }
        if (expression.getValueDateTime() != null) {
            return "@" + DATE_TIME.format(expression.getValueDateTime());
        }
        throw new MalformedExpression("missing value in " + expression);
    }

    private static @NotNull String number(final double value) {
        if (!Double.isFinite(value)) {
            throw new MalformedExpression("cannot write the number " + value + " as code");
        }
        final String text = BigDecimal.valueOf(value).toPlainString();
        return text.contains(".") ? text : text + ".0";
    }

    private static @NotNull String string(final @NotNull String value) {
        final StringBuilder result = new StringBuilder(value.length() + 2).append('"');
        for (final char c : value.toCharArray()) {
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        return result.append('"').toString();
    }

}
