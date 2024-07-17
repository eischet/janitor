package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * List of expressions.
 * Used in function calls and array literals, for example.
 */
public class ExpressionList extends AstNode {

    private final List<Expression> expressionList = new ArrayList<>(4);

    /**
     * Constructor.
     * @param location where
     */
    public ExpressionList(final Location location) {
        super(location);
    }

    /**
     * Add an expression to the list.
     * @param expression what
     * @return this
     */
    public ExpressionList addExpression(final Expression expression) {
        expressionList.add(expression);
        return this;
    }

    /**
     * Get the number of expressions in the list.
     * @return number of expressions
     */
    public int length() {
        return expressionList.size();
    }

    /**
     * Get an expression from the list.
     * @param index which
     * @return the expression
     */
    public Expression get(final int index) {
        return expressionList.get(index);
    }

}
