package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorScriptThrownException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public class ThrowStatement extends Statement implements JsonExportableObject {
    private final Expression expression;

    public ThrowStatement(final Location location, final Expression expression) {
        super(location);
        this.expression = expression;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        final JanitorObject throwable = expression.evaluate(process);
        if (throwable instanceof JanitorRuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new JanitorScriptThrownException(process, throwable);
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginObject().optional("type", simpleClassNameOf(this)).optional("expression", expression).endObject();
    }

}
