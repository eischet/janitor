package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

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
    public @NotNull JList evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        if (elements.isEmpty()) {
            return Janitor.list();
        } else {
            final List<JanitorObject> evaluatedElements = new ArrayList<>(elements.size());
            for (final Expression element : elements) {
                evaluatedElements.add(element.evaluate(process).janitorUnpack());
            }
            return Janitor.list(evaluatedElements);
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject().optional("type", simpleClassNameOf(this));
        producer.key("elements");
        producer.beginArray();
        for (Expression element : elements) {
            element.writeJson(producer);
        }
        producer.endArray();
        producer.endObject();

    }
}
