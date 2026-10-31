// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.functions.tree.*;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Turns a script lambda like {@code item -> item.shortCode == "FOO" && item.stock > 0} into a {@link FilterExpression},
 * so that a DAO can run it as a query instead of loading all entities and calling the lambda for each one.
 * <p>
 * The lambda has one parameter, the entity, and consists of comparisons of its properties ({@code item.field}) with
 * values, combined with {@code and}, {@code or} and {@code not}. Values are everything that does not use the
 * parameter: literals, captured variables, or expressions like {@code limit + 1}, which are evaluated once, before the
 * query runs. Supported conditions, where {@code v} is such a value:
 * </p>
 * <ul>
 *     <li>{@code item.f == v}, {@code !=}, {@code <}, {@code <=}, {@code >}, {@code >=}, with the operands in either
 *     order; comparisons of text are exact, as in scripts; {@code == null} and {@code != null} test for null</li>
 *     <li>{@code item.f ~= v}: equality ignoring case</li>
 *     <li>{@code item.f in [a, b]}, {@code item.f not in list}</li>
 *     <li>{@code item.f.startsWith(v)}, {@code endsWith(v)}, {@code contains(v)} and their variants ending in
 *     {@code IgnoreCase}</li>
 *     <li>{@code item.f ~ "A*"}, {@code "*A"}, {@code "*A*"} and {@code !~}: glob patterns with leading and/or trailing {@code *}</li>
 *     <li>{@code item.f} and {@code not item.f}: a boolean property is true / false</li>
 *     <li>{@code item.f == null or item.f == ""}, {@code item.f != null and item.f != ""}: is (not) empty</li>
 *     <li>{@code item.ref == entity} or {@code item.ref == 42}: a reference to an entity, compared by its ID</li>
 *     <li>{@code item.ref.f == v} etc.: a field of a referenced entity, through any number of references</li>
 * </ul>
 * <p>
 * Anything else is rejected with a {@link MalformedExpression}, instead of being evaluated in memory.
 * {@link FilterExpressionCode} turns a filter expression back into such a lambda.
 * Note that the query follows the rules of SQL for null: e.g. {@code item.code != "FOO"} does not find items without a code.
 * </p>
 */
public final class LambdaFilter {

    private final JanitorScriptProcess process;
    private final JInspectableFunction function;
    private final Predicate<String> isField;

    private LambdaFilter(final JanitorScriptProcess process, final JInspectableFunction function, final Predicate<String> isField) {
        this.process = process;
        this.function = function;
        this.isField = isField;
    }

    /**
     * Converts a lambda to a filter expression.
     *
     * @param process the running script, which evaluates the values
     * @param function the lambda
     * @param isField tells which property names are fields that can be filtered
     * @return the filter expression
     * @throws MalformedExpression if the lambda cannot be converted
     * @throws JanitorRuntimeException if evaluating a value fails
     */
    public static @NotNull FilterExpression toFilter(final @NotNull JanitorScriptProcess process,
                                                     final @NotNull JInspectableFunction function,
                                                     final @NotNull Predicate<String> isField) throws JanitorRuntimeException {
        if (function.getParameterNames().size() != 1) {
            throw new MalformedExpression("a filter needs a function with exactly one parameter, but got " + function.getParameterNames());
        }
        final ExpressionNode tree = function.getExpressionTree()
                .orElseThrow(() -> new MalformedExpression("a filter must be a lambda that consists of a single expression, like 'item -> item.code == \"FOO\"'"));
        return new LambdaFilter(process, function, isField).convert(tree, false);
    }

    private @NotNull FilterExpression convert(final @NotNull ExpressionNode node, final boolean negated) throws JanitorRuntimeException {
        return switch (node) {
            case NotNode not -> convert(not.operand(), !negated);
            case LogicNode logic -> {
                // De Morgan: not (a and b) == (not a) or (not b)
                final boolean and = (logic.operator() == LogicOperator.AND) != negated;
                final List<FilterExpression> parts = new ArrayList<>();
                addFlattened(parts, convert(logic.left(), negated), and);
                addFlattened(parts, convert(logic.right(), negated), and);
                final @Nullable FilterExpression emptiness = emptiness(parts, and);
                if (emptiness != null) {
                    yield emptiness;
                }
                yield and ? FilterExpression.and(parts) : FilterExpression.or(parts);
            }
            case ComparisonNode comparison -> comparison(comparison, negated);
            case MethodCallNode call -> methodCall(call, negated);
            case MemberNode member -> {
                final FilterExpression result = new FilterExpression(field(member), FilterOperator.EQ);
                result.setValueBoolean(!negated);
                yield result;
            }
            default -> throw unsupported(node, "this cannot be used as a filter condition");
        };
    }

    private static void addFlattened(final List<FilterExpression> parts, final FilterExpression part, final boolean and) {
        if (part.isGroup() && part.getLogic() == (and ? FilterLogic.AND : FilterLogic.OR)) {
            parts.addAll(part.getFilters());
        } else {
            parts.add(part);
        }
    }

    private @NotNull FilterExpression comparison(final @NotNull ComparisonNode comparison, final boolean negated) throws JanitorRuntimeException {
        ComparisonOperator operator = comparison.operator();
        ExpressionNode fieldSide = comparison.left();
        ExpressionNode valueSide = comparison.right();
        if (!fieldSide.usesParameters() && valueSide.usesParameters()) {
            // "FOO" == item.code, or 0 < item.stock
            fieldSide = comparison.right();
            valueSide = comparison.left();
            operator = switch (operator) {
                case LESS_THAN -> ComparisonOperator.GREATER_THAN;
                case LESS_THAN_OR_EQUALS -> ComparisonOperator.GREATER_THAN_OR_EQUALS;
                case GREATER_THAN -> ComparisonOperator.LESS_THAN;
                case GREATER_THAN_OR_EQUALS -> ComparisonOperator.LESS_THAN_OR_EQUALS;
                case EQUALS, NOT_EQUALS, EQUALS_IGNORE_CASE -> operator;
                default -> throw unsupported(comparison, "the property must be on the left side of '" + operator + "'");
            };
        }
        if (!(fieldSide instanceof MemberNode member)) {
            throw unsupported(fieldSide, "expected a property of the parameter, like 'item.code'");
        }
        final String field = field(member);
        final JanitorObject value = value(valueSide);
        return switch (operator) {
            case EQUALS -> compare(field, negated ? FilterOperator.NEQ : FilterOperator.EQ, value, false, comparison);
            case NOT_EQUALS -> compare(field, negated ? FilterOperator.EQ : FilterOperator.NEQ, value, false, comparison);
            case EQUALS_IGNORE_CASE -> compare(field, negated ? FilterOperator.NEQ : FilterOperator.EQ, value, true, comparison);
            case LESS_THAN -> compare(field, negated ? FilterOperator.GTE : FilterOperator.LT, value, false, comparison);
            case LESS_THAN_OR_EQUALS -> compare(field, negated ? FilterOperator.GT : FilterOperator.LTE, value, false, comparison);
            case GREATER_THAN -> compare(field, negated ? FilterOperator.LTE : FilterOperator.GT, value, false, comparison);
            case GREATER_THAN_OR_EQUALS -> compare(field, negated ? FilterOperator.LT : FilterOperator.GTE, value, false, comparison);
            case IN -> in(field, value, negated, comparison);
            case NOT_IN -> in(field, value, !negated, comparison);
            case MATCHES -> glob(field, value, negated, comparison);
            case MATCHES_NOT -> glob(field, value, !negated, comparison);
        };
    }

    private @NotNull FilterExpression in(final String field, final JanitorObject value, final boolean negated, final ExpressionNode node) {
        if (!(value instanceof JList list)) {
            throw unsupported(node, "'in' needs a list of values, but got " + simpleClassNameOf(value));
        }
        if (list.size() == 0) {
            throw unsupported(node, "'in' needs at least one value");
        }
        final List<FilterExpression> parts = new ArrayList<>(list.size());
        for (final JanitorObject element : list) {
            parts.add(compare(field, negated ? FilterOperator.NEQ : FilterOperator.EQ, element.janitorUnpack(), false, node));
        }
        if (parts.size() == 1) {
            return parts.getFirst();
        }
        return negated ? FilterExpression.and(parts) : FilterExpression.or(parts);
    }

    private @NotNull FilterExpression glob(final String field, final JanitorObject value, final boolean negated, final ExpressionNode node) {
        if (!(value instanceof JString string)) {
            throw unsupported(node, "only text patterns can be used with '~', but got " + simpleClassNameOf(value));
        }
        final String pattern = string.janitorGetHostValue();
        final boolean leading = pattern.startsWith("*");
        final boolean trailing = pattern.length() > 1 && pattern.endsWith("*");
        final String text = pattern.substring(leading ? 1 : 0, pattern.length() - (trailing ? 1 : 0));
        if (text.isEmpty() || text.contains("*") || text.contains("?")) {
            throw unsupported(node, "only patterns like \"A*\", \"*A\" or \"*A*\" can be used in a filter, but got \"" + pattern + "\"");
        }
        final FilterOperator operator;
        if (leading && trailing) {
            operator = negated ? FilterOperator.DOESNOTCONTAIN : FilterOperator.CONTAINS;
        } else if (negated && (leading || trailing)) {
            throw unsupported(node, "there is no negated form of \"" + pattern + "\" in a filter");
        } else if (leading) {
            operator = FilterOperator.ENDSWITH;
        } else if (trailing) {
            operator = FilterOperator.STARTSWITH;
        } else {
            operator = negated ? FilterOperator.NEQ : FilterOperator.EQ;
        }
        return text(field, operator, text, false);
    }

    private @NotNull FilterExpression methodCall(final @NotNull MethodCallNode call, final boolean negated) throws JanitorRuntimeException {
        if (!(call.target() instanceof MemberNode member)) {
            throw unsupported(call, "expected a method call on a property of the parameter, like 'item.name.startsWith(\"A\")'");
        }
        if (call.arguments().size() != 1 || call.arguments().getFirst().name() != null) {
            throw unsupported(call, "'" + call.name() + "' needs exactly one argument in a filter");
        }
        final String field = field(member);
        final JanitorObject value = value(call.arguments().getFirst().value());
        if (!(value instanceof JString string)) {
            throw unsupported(call, "'" + call.name() + "' needs a text argument in a filter, but got " + simpleClassNameOf(value));
        }
        final String text = string.janitorGetHostValue();
        return switch (call.name()) {
            case "contains" -> text(field, negated ? FilterOperator.DOESNOTCONTAIN : FilterOperator.CONTAINS, text, false);
            case "containsIgnoreCase" -> text(field, negated ? FilterOperator.DOESNOTCONTAIN : FilterOperator.CONTAINS, text, true);
            case "startsWith", "startsWithIgnoreCase", "endsWith", "endsWithIgnoreCase" -> {
                if (negated) {
                    throw unsupported(call, "there is no negated form of '" + call.name() + "' in a filter");
                }
                final FilterOperator operator = call.name().startsWith("startsWith") ? FilterOperator.STARTSWITH : FilterOperator.ENDSWITH;
                yield text(field, operator, text, call.name().endsWith("IgnoreCase"));
            }
            default -> throw unsupported(call, "the method '" + call.name() + "' cannot be used in a filter");
        };
    }

    /**
     * Recognizes the script forms of "is empty", {@code item.f == null or item.f == ""}, and of "is not empty",
     * {@code item.f != null and item.f != ""}, which a filter has operators for.
     */
    private static @Nullable FilterExpression emptiness(final List<FilterExpression> parts, final boolean and) {
        if (parts.size() != 2 || !parts.get(0).isExpression() || !parts.get(1).isExpression()) {
            return null;
        }
        final FilterExpression nullTest = parts.get(0);
        final FilterExpression emptyTest = parts.get(1);
        if (!Objects.equals(nullTest.getField(), emptyTest.getField()) || !"".equals(emptyTest.getValueString())
            || Boolean.TRUE.equals(emptyTest.getIgnoreCase())) {
            return null;
        }
        if (!and && nullTest.getOperator() == FilterOperator.ISNULL && emptyTest.getOperator() == FilterOperator.EQ) {
            return new FilterExpression(nullTest.getField(), FilterOperator.ISEMPTY);
        }
        if (and && nullTest.getOperator() == FilterOperator.ISNOTNULL && emptyTest.getOperator() == FilterOperator.NEQ) {
            return new FilterExpression(nullTest.getField(), FilterOperator.ISNOTEMPTY);
        }
        return null;
    }

    private static @NotNull FilterExpression text(final String field, final FilterOperator operator, final String text, final boolean ignoreCase) {
        final FilterExpression result = new FilterExpression(field, operator, ignoreCase);
        result.setValueString(text);
        return result;
    }

    private @NotNull FilterExpression compare(final String field, final FilterOperator operator, final JanitorObject value,
                                              final boolean ignoreCase, final ExpressionNode node) {
        if (value == JNull.NULL || value instanceof ForeignKeyNull<?>) {
            return switch (operator) {
                case EQ -> new FilterExpression(field, FilterOperator.ISNULL);
                case NEQ -> new FilterExpression(field, FilterOperator.ISNOTNULL);
                default -> throw unsupported(node, "null can only be compared with == and !=");
            };
        }
        if (ignoreCase && !(value instanceof JString)) {
            throw unsupported(node, "'~=' can only compare text");
        }
        final FilterExpression result = new FilterExpression(field, operator, ignoreCase ? Boolean.TRUE : null);
        switch (value) {
            case JString string -> result.setValueString(string.janitorGetHostValue());
            case JInt integer -> result.setValueLong(integer.janitorGetHostValue());
            case JFloat number -> result.setValueDouble(number.janitorGetHostValue());
            case JBool bool -> result.setValueBoolean(bool.janitorIsTrue());
            case JDate date -> result.setValueDate(date.janitorGetHostValue());
            case JDateTime dateTime -> result.setValueDateTime(dateTime.janitorGetHostValue());
            // a reference is compared by the ID of the entity, like a lookup field in a filter UI:
            case OrmEntity entity -> result.setValueLong(entity.getId());
            case ForeignKey<?> reference -> result.setValueLong(reference.getId());
            default -> throw unsupported(node, "a " + simpleClassNameOf(value) + " cannot be used as a value in a filter");
        }
        return result;
    }

    /**
     * Returns the field, or the path through references, e.g. "product.shortCode" for {@code item.product.shortCode}.
     */
    private @NotNull String field(final @NotNull MemberNode member) {
        final String path = path(member);
        if (!isField.test(path)) {
            throw unsupported(member, "'" + path + "' is not a field that can be used in a filter");
        }
        return path;
    }

    private @NotNull String path(final @NotNull MemberNode member) {
        return switch (member.target()) {
            case ParameterNode ignored -> member.name();
            case MemberNode parent -> path(parent) + "." + member.name();
            default -> throw unsupported(member, "expected a property of the parameter, like 'item.code' or 'item.product.code'");
        };
    }

    private @NotNull JanitorObject value(final @NotNull ExpressionNode node) throws JanitorRuntimeException {
        if (node.usesParameters()) {
            throw unsupported(node, "expected a value that does not depend on the parameter");
        }
        return function.evaluate(process, node).janitorUnpack();
    }

    private static MalformedExpression unsupported(final @NotNull ExpressionNode node, final @NotNull String message) {
        return new MalformedExpression(message + where(node));
    }

    private static String where(final @NotNull ExpressionNode node) {
        return node.location() == null ? "" : " (line " + node.location().getLine() + ")";
    }

}
