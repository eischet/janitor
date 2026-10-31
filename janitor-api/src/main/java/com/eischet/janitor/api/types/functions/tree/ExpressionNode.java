// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.Nullable;

/**
 * A node of the expression tree of an {@link JInspectableFunction}.
 * The tree describes the code only; values are produced by {@link JInspectableFunction#evaluate}.
 * Nodes are compared by identity when evaluated, so pass the very nodes of the function's tree.
 */
public sealed interface ExpressionNode permits ParameterNode, NameNode, ConstantNode, ListNode, MemberNode, MethodCallNode,
        FunctionCallNode, ComparisonNode, LogicNode, NotNode, ArithmeticNode, OpaqueNode {

    /**
     * @return where the node is in the script source
     */
    @Nullable Location location();

    /**
     * @return true if this node or one of its children refers to a parameter of the function
     */
    default boolean usesParameters() {
        return switch (this) {
            case ParameterNode ignored -> true;
            case NameNode ignored -> false;
            case ConstantNode ignored -> false;
            case ListNode list -> list.elements().stream().anyMatch(ExpressionNode::usesParameters);
            case MemberNode member -> member.target().usesParameters();
            case MethodCallNode call -> call.target().usesParameters() || call.arguments().stream().anyMatch(a -> a.value().usesParameters());
            case FunctionCallNode call -> call.arguments().stream().anyMatch(a -> a.value().usesParameters());
            case ComparisonNode comparison -> comparison.left().usesParameters() || comparison.right().usesParameters();
            case LogicNode logic -> logic.left().usesParameters() || logic.right().usesParameters();
            case NotNode not -> not.operand().usesParameters();
            case ArithmeticNode arithmetic -> arithmetic.left().usesParameters() || arithmetic.right().usesParameters();
            case OpaqueNode opaque -> opaque.mayUseParameters();
        };
    }

}
