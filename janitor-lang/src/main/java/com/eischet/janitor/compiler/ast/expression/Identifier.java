package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.AstNode;

public class Identifier extends AstNode implements Expression {
    private final String text;

    public Identifier(final Location location, final String text) {
        super(location);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorNameException {
        runningScript.setCurrentLocation(getLocation());
        final JanitorObject v = runningScript.lookup(text);
        if (v == null) {
            throw new JanitorNameException(runningScript, String.format("name '%s' is not defined", text));
        } else {
            return v;
        }
    }

}
