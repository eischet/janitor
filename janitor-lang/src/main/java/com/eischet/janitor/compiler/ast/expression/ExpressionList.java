package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;

import java.util.ArrayList;
import java.util.List;

public class ExpressionList extends AstNode {

    private final List<Expression> expressionList = new ArrayList<>(4);

    public ExpressionList(final Location location) {
        super(location);
    }

    public ExpressionList addExpression(final Expression expression) {
        expressionList.add(expression);
        return this;
    }

    public int length() {
        return expressionList.size();
    }

    public Expression get(final int index) {
        return expressionList.get(index);
    }

}
