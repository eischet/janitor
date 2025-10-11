package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import java.util.*;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * List of expressions.
 * Used in function calls and array literals, for example.
 */
public class ArgumentList extends AstNode implements JsonExportableObject {

    private final List<Expression> expressionList = new LinkedList<>();
    private final Map<String, Expression> namedExpressions = new HashMap<>();

    /**
     * Constructor.
     * @param location where
     */
    public ArgumentList(final Location location) {
        super(location);
    }

    /**
     * Add an expression to the list.
     * @param expression what
     * @return this
     */
    public ArgumentList addExpression(final Expression expression) {
        expressionList.add(expression);
        return this;
    }

    public ArgumentList addNamedExpression(final String name, final Expression expression) {
        namedExpressions.put(name, expression);
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

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("args", expressionList)
                .endObject();

    }

}
