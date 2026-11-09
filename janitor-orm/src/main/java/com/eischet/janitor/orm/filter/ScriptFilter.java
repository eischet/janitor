// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.functions.tree.JInspectableFunction;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Turns the condition that a script passes to {@code dao.select(...)} or {@code dao.count(...)} into a {@link FilterExpression}.
 * <p>
 * The condition can be given in three ways:
 * <ul>
 *     <li>as a lambda like {@code item -> item.code == "FOO"}, see {@link LambdaFilter};</li>
 *     <li>as a map in the JSON form of a filter expression, e.g. {@code {field: "code", operator: "eq", valueString: "FOO"}}
 *     or {@code {logic: "and", filters: [...]}};</li>
 *     <li>as a {@link FilterExpression} object.</li>
 * </ul>
 * Maps and filter expressions must be complete, and every field they use must be one that the DAO can filter by.
 * They may use pseudo fields (see {@link FilterRewrite}), e.g. {@code {field: "open", operator: "eq", valueBoolean: true}}; the DAO
 * rewrites them when it builds the query.
 */
public final class ScriptFilter {

    private ScriptFilter() {
    }

    /**
     * Converts a script argument to a filter expression.
     *
     * @param process the running script, which evaluates the values of a lambda
     * @param argument the argument: a lambda, a map or a filter expression
     * @param isField tells which fields, or paths through references, can be filtered
     * @param lambdaExample an example lambda for the error message, like {@code item -> item.code == "FOO"}
     * @return the filter expression, which the caller may change
     * @throws MalformedExpression if the argument cannot be used as a filter
     * @throws JanitorRuntimeException if evaluating a value of a lambda fails
     */
    public static @NotNull FilterExpression toFilter(final @NotNull JanitorScriptProcess process,
                                                     final @NotNull JanitorObject argument,
                                                     final @NotNull Predicate<String> isField,
                                                     final @NotNull String lambdaExample) throws JanitorRuntimeException {
        return switch (argument) {
            case JInspectableFunction function -> LambdaFilter.toFilter(process, function, isField);
            case JMap map -> checked(fromMap(map), isField);
            case FilterExpression expression -> checked(expression.deepCopy(), isField);
            default -> throw new MalformedExpression("expecting a lambda like '" + lambdaExample + "' or a filter map like "
                    + "{field: \"code\", operator: \"eq\", valueString: \"FOO\"}, but found " + simpleClassNameOf(argument));
        };
    }

    private static @NotNull FilterExpression fromMap(final @NotNull JMap map) {
        try {
            return FilterExpression.fromJson(Janitor.current().writeJson(map::writeJson));
        } catch (JsonException e) {
            throw new MalformedExpression("the filter map cannot be read as a filter expression: " + e.getMessage(), e);
        }
    }

    private static @NotNull FilterExpression checked(final @NotNull FilterExpression filter, final @NotNull Predicate<String> isField) {
        if (filter.isIncomplete()) {
            throw new MalformedExpression("the filter is incomplete: " + filter);
        }
        checkFields(filter, isField);
        return filter;
    }

    private static void checkFields(final @NotNull FilterExpression filter, final @NotNull Predicate<String> isField) {
        if (filter.isGroup()) {
            for (final FilterExpression child : filter.getFilters()) {
                checkFields(child, isField);
            }
        } else if (filter.getField() == null || !isField.test(filter.getField())) {
            throw new MalformedExpression("'" + filter.getField() + "' is not a field that can be filtered");
        }
    }

}
