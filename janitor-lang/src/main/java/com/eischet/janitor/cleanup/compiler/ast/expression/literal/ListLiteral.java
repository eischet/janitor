package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JList;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

public class ListLiteral extends Literal {
    private final ImmutableList<Expression> elements;

    public ListLiteral(final Location location, ImmutableList<Expression> elements) {
        super(location);
        this.elements = elements;
    }

    @Override
    public JList evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        if (elements.isEmpty()) {
            return new JList();
        } else {
            final MutableList<JanitorObject> evaluatedElements = Lists.mutable.empty();
            for (final Expression element : elements) {
                evaluatedElements.add(element.evaluate(runningScript).janitorUnpack());
            }
            return new JList(evaluatedElements);
        }
    }

}
