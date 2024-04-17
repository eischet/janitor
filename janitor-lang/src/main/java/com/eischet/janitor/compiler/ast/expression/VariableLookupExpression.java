package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;

public class VariableLookupExpression extends AstNode implements Expression {

    private final String variableName;

    public VariableLookupExpression(final Location location, final String variableName) {
        super(location);
        this.variableName = variableName;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        runningScript.setCurrentLocation(getLocation());
        return runningScript.lookup(variableName).janitorUnpack();
    }

}
