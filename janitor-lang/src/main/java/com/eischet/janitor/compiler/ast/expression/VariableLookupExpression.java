package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;

/**
 * Variable lookup expression.
 */
public class VariableLookupExpression extends AstNode implements Expression {

    private final String variableName;

    /**
     * Constructor.
     * @param location where
     * @param variableName what
     */
    public VariableLookupExpression(final Location location, final String variableName) {
        super(location);
        this.variableName = variableName;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) {
        process.setCurrentLocation(getLocation());
        return process.lookup(variableName).janitorUnpack();
    }

}
