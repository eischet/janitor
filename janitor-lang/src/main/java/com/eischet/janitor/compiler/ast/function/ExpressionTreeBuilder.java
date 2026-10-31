// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.function;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.tree.*;
import com.eischet.janitor.compiler.ast.expression.ArgumentList;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.compiler.ast.expression.binary.*;
import com.eischet.janitor.compiler.ast.expression.literal.*;
import com.eischet.janitor.compiler.ast.expression.unary.LogicalNot;
import com.eischet.janitor.compiler.ast.statement.FunctionCallStatement;
import com.eischet.janitor.compiler.ast.statement.MemberAccessExpression;
import com.eischet.janitor.compiler.ast.statement.MemberCallExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Converts the AST of a function's return expression into the public {@link ExpressionNode} tree, remembering which
 * AST expression each node came from, so that parts of the tree can be evaluated later.
 */
public final class ExpressionTreeBuilder {

    /**
     * The result of a conversion.
     * @param root the root of the tree
     * @param sources the AST expression of each node, by identity
     */
    public record Result(@NotNull ExpressionNode root, @NotNull Map<ExpressionNode, Expression> sources) {
    }

    private final List<String> parameterNames;
    private final Map<ExpressionNode, Expression> sources = new IdentityHashMap<>();

    private ExpressionTreeBuilder(final List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }

    /**
     * Converts an expression.
     * @param expression the expression
     * @param parameterNames the names of the function's parameters
     * @return the tree
     */
    public static @NotNull Result build(final @NotNull Expression expression, final @NotNull List<String> parameterNames) {
        final ExpressionTreeBuilder builder = new ExpressionTreeBuilder(parameterNames);
        final ExpressionNode root = builder.convert(expression);
        return new Result(root, builder.sources);
    }

    private @NotNull ExpressionNode convert(final @NotNull Expression expression) {
        final ExpressionNode node = createNode(expression);
        sources.put(node, expression);
        return node;
    }

    private @NotNull ExpressionNode createNode(final @NotNull Expression expression) {
        final var location = expression.getLocation();
        return switch (expression) {
            case Identifier identifier -> {
                final int index = parameterNames.indexOf(identifier.getText());
                yield index >= 0 ? new ParameterNode(location, identifier.getText(), index) : new NameNode(location, identifier.getText());
            }
            case StringLiteral ignored -> constant(expression);
            case IntegerLiteral ignored -> constant(expression);
            case FloatLiteral ignored -> constant(expression);
            case BooleanLiteral ignored -> constant(expression);
            case NullLiteral ignored -> constant(expression);
            case DateLiteral ignored -> constant(expression);
            case DateTimeLiteral ignored -> constant(expression);
            case DurationLiteral ignored -> constant(expression);
            case RegexLiteral ignored -> constant(expression);
            // these depend on the clock, not on the parameters:
            case TodayLiteral ignored -> new OpaqueNode(location, simpleClassNameOf(expression), false);
            case NowLiteral ignored -> new OpaqueNode(location, simpleClassNameOf(expression), false);
            case ListLiteral list -> new ListNode(location, list.getElements().stream().map(this::convert).toList());
            case MemberAccessExpression member -> new MemberNode(location, convert(member.getExpression()), member.getIdentifier(), member.isGuarded());
            case MemberCallExpression call -> new MethodCallNode(location, convert(call.getExpression()), call.getIdentifier(), arguments(call.getArgs()), call.isGuarded());
            case FunctionCallStatement call when call.getOnExpression() != null ->
                    new MethodCallNode(location, convert(call.getOnExpression()), call.getFunctionName(), arguments(call.getExpressionList()), false);
            case FunctionCallStatement call -> new FunctionCallNode(location, call.getFunctionName(), arguments(call.getExpressionList()));
            case Equality op -> comparison(op, ComparisonOperator.EQUALS);
            case NonEquality op -> comparison(op, ComparisonOperator.NOT_EQUALS);
            case CaseInsensitiveEquality op -> comparison(op, ComparisonOperator.EQUALS_IGNORE_CASE);
            case LessThan op -> comparison(op, ComparisonOperator.LESS_THAN);
            case LessThanOrEquals op -> comparison(op, ComparisonOperator.LESS_THAN_OR_EQUALS);
            case GreaterThan op -> comparison(op, ComparisonOperator.GREATER_THAN);
            case GreaterThanOrEquals op -> comparison(op, ComparisonOperator.GREATER_THAN_OR_EQUALS);
            case MatchesGlob op -> comparison(op, ComparisonOperator.MATCHES);
            case MatchesNotGlob op -> comparison(op, ComparisonOperator.MATCHES_NOT);
            case In op -> comparison(op, ComparisonOperator.IN);
            case NotIn op -> comparison(op, ComparisonOperator.NOT_IN);
            case LogicAnd op -> new LogicNode(location, LogicOperator.AND, convert(op.getLeft()), convert(op.getRight()));
            case LogicOr op -> new LogicNode(location, LogicOperator.OR, convert(op.getLeft()), convert(op.getRight()));
            case LogicalNot op -> new NotNode(location, convert(op.getParameter()));
            case Addition op -> arithmetic(op, ArithmeticOperator.ADD);
            case Subtraction op -> arithmetic(op, ArithmeticOperator.SUBTRACT);
            case Multiplication op -> arithmetic(op, ArithmeticOperator.MULTIPLY);
            case Division op -> arithmetic(op, ArithmeticOperator.DIVIDE);
            case Modulo op -> arithmetic(op, ArithmeticOperator.MODULO);
            // We cannot look inside, so we have to assume the worst:
            default -> new OpaqueNode(location, simpleClassNameOf(expression), true);
        };
    }

    /**
     * Literals with a fixed value evaluate without looking at the process, so we can take their value right away.
     */
    private @NotNull ExpressionNode constant(final @NotNull Expression literal) {
        try {
            return new ConstantNode(literal.getLocation(), literal.evaluate(null));
        } catch (JanitorRuntimeException | RuntimeException e) {
            return new OpaqueNode(literal.getLocation(), simpleClassNameOf(literal), false);
        }
    }

    private @NotNull ExpressionNode comparison(final BinaryOperation op, final ComparisonOperator operator) {
        return new ComparisonNode(op.getLocation(), operator, convert(op.getLeft()), convert(op.getRight()));
    }

    private @NotNull ExpressionNode arithmetic(final BinaryOperation op, final ArithmeticOperator operator) {
        return new ArithmeticNode(op.getLocation(), operator, convert(op.getLeft()), convert(op.getRight()));
    }

    private @NotNull List<CallArgument> arguments(final @Nullable ArgumentList args) {
        if (args == null) {
            return List.of();
        }
        final List<CallArgument> result = new ArrayList<>(args.length());
        for (final ArgumentList.Argument argument : args.getArguments()) {
            result.add(new CallArgument(argument.getName(), convert(argument.getExpression())));
        }
        return result;
    }

}
