package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public class ExpressionList extends AstNode {

    private final MutableList<Expression> expressionList = Lists.mutable.empty();

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
