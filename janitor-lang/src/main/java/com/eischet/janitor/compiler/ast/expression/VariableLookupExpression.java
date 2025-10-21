package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Variable lookup expression.
 */
public class VariableLookupExpression extends AstNode implements Expression, JsonExportableObject {

    private final String variableName;

    /**
     * Constructor.
     * @param location where
     * @param variableName what
     */
    public VariableLookupExpression(final Location location, final String variableName) {
        super(location);
        this.variableName = variableName;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) {
        process.setCurrentLocation(getLocation());
        return process.lookup(variableName).janitorUnpack();
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("name", variableName)
                .endObject();
    }
}
