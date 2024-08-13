package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import java.util.List;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Qualified name, i.e. a multi-part identifier: foo.bar.baz.
 */
public class QualifiedName extends AstNode implements Expression {
    private final List<String> parts;

    /**
     * Constructor.
     *
     * @param location where
     * @param parts    what
     */
    public QualifiedName(final Location location, final List<String> parts) {
        super(location);
        this.parts = parts;
    }

    /**
     * Get the parts.
     *
     * @return the parts
     */
    public List<String> getParts() {
        return parts;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        if (parts.isEmpty()) {
            throw new JanitorNameException(process, "a qualified name can not be empty");
        } else if (parts.size() == 1) {
            final JanitorObject v = process.lookup(parts.get(0));
            if (v == null) {
                throw new JanitorNameException(process, String.format("name '%s' is not defined", parts.get(0)));
            } else {
                return v;
            }
        } else {
            throw new JanitorNameException(process, "multipart qualified names are not yet implemented: " + parts);
            // LATER: implement multipart qualified names. I don't remember what this should be doing, though, so maybe this is a phantom feature....
        }
    }

    @Override
    public String toString() {
        return "QualifiedName " + parts;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject().optional("type", simpleClassNameOf(this));
        producer.key("parts").beginArray();
        for (String part : parts) {
            producer.value(part);
        }
        producer.endArray();
        producer.endObject();
    }

}
