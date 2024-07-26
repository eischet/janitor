package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;

import java.util.List;

/**
 * Qualified name, i.e. a multi-part identifier: foo.bar.baz.
 */
public class QualifiedName extends AstNode implements Expression {
    private final List<String> parts;

    /**
     * Constructor.
     * @param location where
     * @param parts what
     */
    public QualifiedName(final Location location, final List<String> parts) {
        super(location);
        this.parts = parts;
    }

    /**
     * Get the parts.
     * @return the parts
     */
    public List<String> getParts() {
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
            // LATER: implement multipart qualified names. I don't remember what this should be doing, though, so maybe this is a phantom feature....
        }
    }

    @Override
    public String toString() {
        return "QualifiedName " + parts;
    }
}
