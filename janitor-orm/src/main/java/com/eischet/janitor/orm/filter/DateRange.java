// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.eischet.janitor.orm.filter.FilterExpression.and;
import static com.eischet.janitor.orm.filter.FilterExpression.from;
import static com.eischet.janitor.orm.filter.FilterExpression.or;
import static com.eischet.janitor.orm.filter.FilterOperator.GTE;
import static com.eischet.janitor.orm.filter.FilterOperator.LT;

/**
 * Turns a condition on the date of a date-time field into a range of times, see {@link FilterRewrite#dateOf}.
 */
final class DateRange {

    private DateRange() {
    }

    static @NotNull FilterExpression of(final @NotNull String field, final @NotNull FilterExpression condition) {
        final FilterOperator operator = condition.getOperator();
        if (operator == null) {
            throw new MalformedExpression("missing operator in expression " + condition);
        }
        return switch (operator) {
            case ISNULL, ISEMPTY -> new FilterExpression(field, FilterOperator.ISNULL);
            case ISNOTNULL, ISNOTEMPTY -> new FilterExpression(field, FilterOperator.ISNOTNULL);
            case EQ, NEQ, LT, LTE, GT, GTE -> {
                final LocalDateTime start = dateOf(condition).atStartOfDay();
                final LocalDateTime nextDay = start.plusDays(1);
                yield switch (operator) {
                    case EQ -> and(List.of(from(field, GTE, start), from(field, LT, nextDay)));
                    case NEQ -> or(List.of(from(field, LT, start), from(field, GTE, nextDay)));
                    case LT -> from(field, LT, start);
                    case LTE -> from(field, LT, nextDay);
                    case GT -> from(field, GTE, nextDay);
                    default -> from(field, GTE, start);
                };
            }
            default -> throw new MalformedExpression("'" + condition.getField() + "' is a date and cannot be compared with '" + operator.getCode() + "'");
        };
    }

    private static @NotNull LocalDate dateOf(final @NotNull FilterExpression condition) {
        if (condition.getValueDate() != null) {
            return condition.getValueDate();
        }
        if (condition.getValueDateTime() != null) {
            return condition.getValueDateTime().toLocalDate();
        }
        throw new MalformedExpression("'" + condition.getField() + "' needs a date to compare with");
    }

}
