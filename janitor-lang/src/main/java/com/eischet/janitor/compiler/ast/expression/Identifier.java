package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Identifier, e.g. a variable name or a function name.
 */
public class Identifier extends AstNode implements Expression {
    private final String text;

    /**
     * Constructor.
     * @param location where
     * @param text what
     */
    public Identifier(final Location location, final String text) {
        super(location);
        this.text = text;
    }

    /**
     * Get the text.
     * @return the text
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "=" + text + "@" + getLocation();
    }


    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorNameException {
        process.setCurrentLocation(getLocation());
        final JanitorObject v = process.lookup(text);
        if (v == null) {
            throw new JanitorNameException(process, String.format("name '%s' is not defined", text));
        } else {
            return v;
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("text", text)
                .endObject();

    }

}
