package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JList;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * List literal.
 */
public class ListLiteral extends Literal {
    private final List<Expression> elements;

    /**
     * Constructor.
     * @param location where
     * @param elements what
     */
    public ListLiteral(final Location location, List<Expression> elements) {
        super(location);
        this.elements = elements;
    }

    @Override
    public JList evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        if (elements.isEmpty()) {
            return runningScript.getEnvironment().getBuiltins().list();
        } else {
            final List<JanitorObject> evaluatedElements = new ArrayList<>(elements.size());
            for (final Expression element : elements) {
                evaluatedElements.add(element.evaluate(runningScript).janitorUnpack());
            }
            return runningScript.getEnvironment().getBuiltins().list(evaluatedElements);
        }
    }

}
