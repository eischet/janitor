// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.functions.tree.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** The host's view of lambdas and functions as expression trees, see {@link JInspectableFunction}. */
public class LambdaExpressionTreeTestCase extends JanitorTest {

    /**
     * Runs the script with a host function "inspect(f)", which renders the tree of f, and also the values of every
     * part of it that does not use the parameters.
     */
    private List<String> inspect(final String script) throws Exception {
        final List<String> result = new ArrayList<>();
        evaluate(script, g -> g.bindF("inspect", (JCallable) (process, arguments) -> {
            final JInspectableFunction function = (JInspectableFunction) arguments.require(1).get(0);
            final ExpressionNode tree = function.getExpressionTree().orElse(null);
            result.add(tree == null ? "-" : render(tree));
            if (tree != null) {
                for (final ExpressionNode node : parameterFreeParts(tree, new ArrayList<>())) {
                    result.add(render(node) + " = " + function.evaluate(process, node).janitorToString());
                }
            }
            return Janitor.NULL;
        }));
        return result;
    }

    /** Collects the topmost parts of the tree that do not use the parameters, except constants. */
    private static List<ExpressionNode> parameterFreeParts(final ExpressionNode node, final List<ExpressionNode> into) {
        if (!node.usesParameters()) {
            if (!(node instanceof ConstantNode)) {
                into.add(node);
            }
            return into;
        }
        switch (node) {
            case MemberNode member -> parameterFreeParts(member.target(), into);
            case MethodCallNode call -> {
                parameterFreeParts(call.target(), into);
                call.arguments().forEach(a -> parameterFreeParts(a.value(), into));
            }
            case ComparisonNode c -> {
                parameterFreeParts(c.left(), into);
                parameterFreeParts(c.right(), into);
            }
            case LogicNode l -> {
                parameterFreeParts(l.left(), into);
                parameterFreeParts(l.right(), into);
            }
            case NotNode not -> parameterFreeParts(not.operand(), into);
            case ArithmeticNode a -> {
                parameterFreeParts(a.left(), into);
                parameterFreeParts(a.right(), into);
            }
            default -> {
            }
        }
        return into;
    }

    private static String render(final ExpressionNode node) {
        return switch (node) {
            case ParameterNode p -> "$" + p.name() + "#" + p.index();
            case NameNode n -> n.name();
            case ConstantNode c -> c.value().janitorClassName() + ":" + c.value().janitorToString();
            case ListNode l -> l.elements().stream().map(LambdaExpressionTreeTestCase::render).collect(Collectors.joining(", ", "[", "]"));
            case MemberNode m -> render(m.target()) + (m.guarded() ? "?." : ".") + m.name();
            case MethodCallNode m -> render(m.target()) + "." + m.name() + m.arguments().stream()
                    .map(a -> (a.name() == null ? "" : a.name() + "=") + render(a.value())).collect(Collectors.joining(", ", "(", ")"));
            case FunctionCallNode f -> f.name() + f.arguments().stream().map(a -> render(a.value())).collect(Collectors.joining(", ", "(", ")"));
            case ComparisonNode c -> "(" + render(c.left()) + " " + c.operator() + " " + render(c.right()) + ")";
            case LogicNode l -> "(" + render(l.left()) + " " + l.operator() + " " + render(l.right()) + ")";
            case NotNode n -> "NOT " + render(n.operand());
            case ArithmeticNode a -> "(" + render(a.left()) + " " + a.operator() + " " + render(a.right()) + ")";
            case OpaqueNode o -> "?" + o.kind();
        };
    }

    @Test
    void comparesMemberWithConstant() throws Exception {
        assertEquals(List.of("($item#0.shortCode EQUALS string:FOO)"),
                inspect("inspect(item -> item.shortCode == \"FOO\")"));
    }

    @Test
    void capturedVariablesCanBeEvaluated() throws Exception {
        assertEquals(List.of("($item#0.shortCode EQUALS code)", "code = BAR"),
                inspect("code = \"BAR\"; inspect(item -> item.shortCode == code)"));
    }

    @Test
    void capturedScopeIsTheOneOfTheClosure() throws Exception {
        assertEquals(List.of("($i#0.stock GREATER_THAN (c ADD int:1))", "(c ADD int:1) = 3",
                        "($i#0.stock GREATER_THAN (c ADD int:1))", "(c ADD int:1) = 11"),
                inspect("""
                        function f(c) { return i -> i.stock > c + 1; }
                        a = f(2);
                        b = f(10);
                        inspect(a);
                        inspect(b);
                        """));
    }

    @Test
    void logicAndOtherOperators() throws Exception {
        assertEquals(List.of("((($x#0.a NOT_EQUALS null:null) AND NOT ($x#0.b IN [string:A, string:B])) OR $x#0.name.startsWith(string:X))",
                        "[string:A, string:B] = [A, B]"),
                inspect("inspect(x -> x.a != null && not (x.b in [\"A\", \"B\"]) || x.name.startsWith(\"X\"))"));
        assertEquals(List.of("(($x#0.a EQUALS_IGNORE_CASE string:foo) AND ($x#0.b MATCHES string:F*))"),
                inspect("inspect(x -> x.a ~= \"foo\" and x.b ~ \"F*\")"));
    }

    @Test
    void severalParameters() throws Exception {
        assertEquals(List.of("($a#0 LESS_THAN $b#1)"), inspect("inspect((a, b) -> a < b)"));
    }

    @Test
    void clockLiteralsAreEvaluatedButNotConstant() throws Exception {
        final List<String> result = inspect("inspect(x -> x.created >= @today)");
        assertEquals("($x#0.created GREATER_THAN_OR_EQUALS ?TodayLiteral)", result.get(0));
        assertEquals(2, result.size());
        assertTrue(result.get(1).startsWith("?TodayLiteral = @"), result.get(1));
    }

    @Test
    void functionsWithASingleReturnHaveATree() throws Exception {
        assertEquals(List.of("($x#0.a GREATER_THAN int:1)", "($x#0.a GREATER_THAN int:1)", "-"),
                inspect("""
                        function p(x) { return x.a > 1; }
                        inspect(p);
                        inspect(x -> { return x.a > 1; });
                        inspect(x -> { y = x.a; return y > 1; });
                        """));
    }

    @Test
    void cannotEvaluateWhatUsesParameters() throws Exception {
        final List<Throwable> errors = new ArrayList<>();
        evaluate("inspect(x -> x.a == 1)", g -> g.bindF("inspect", (JCallable) (process, arguments) -> {
            final JInspectableFunction function = (JInspectableFunction) arguments.get(0);
            final ExpressionNode tree = function.getExpressionTree().orElseThrow();
            errors.add(assertThrows(IllegalArgumentException.class, () -> function.evaluate(process, tree)));
            errors.add(assertThrows(IllegalArgumentException.class,
                    () -> function.evaluate(process, new NameNode(null, "x")))); // not a node of this tree
            return Janitor.NULL;
        }));
        assertEquals(2, errors.size());
    }

    @Test
    void lambdasAreStillCallable() throws Exception {
        final JanitorObject result = evaluate("""
                code = "B";
                return call(item -> item == code);
                """, g -> g.bindF("call", (JCallable) (process, arguments) -> {
            final JCallable function = (JCallable) arguments.get(0);
            return function.call(process, new JCallArgs("lambda", process, List.of(Janitor.string("B"))));
        }));
        assertEquals(Janitor.TRUE, result);
    }

}
