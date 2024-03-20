package com.eischet.janitor.cleanup.compiler.ast.expression;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.AstNode;

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
