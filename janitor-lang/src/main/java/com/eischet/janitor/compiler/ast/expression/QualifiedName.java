package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.AstNode;
import org.eclipse.collections.api.list.ImmutableList;

public class QualifiedName extends AstNode implements Expression {
    private final ImmutableList<String> parts;

    public QualifiedName(final Location location, final ImmutableList<String> parts) {
        super(location);
        this.parts = parts;
    }

    public ImmutableList<String> getParts() {
        return parts;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        runningScript.setCurrentLocation(getLocation());
        if (parts.isEmpty()) {
            throw new JanitorNameException(runningScript, "a qualified name can not be empty");
        } else if (parts.size() == 1) {
            final JanitorObject v = runningScript.lookup(parts.get(0));
            if (v == null) {
                throw new JanitorNameException(runningScript, String.format("name '%s' is not defined", parts.get(0)));
            } else {
                return v;
            }
        } else {
            throw new JanitorNameException(runningScript, "multipart qualified names are not yet implemented: " + parts);
            // LATER: implement multipart qualified names
        }
    }

    @Override
    public String toString() {
        return "QualifiedName " + parts;
    }
}
