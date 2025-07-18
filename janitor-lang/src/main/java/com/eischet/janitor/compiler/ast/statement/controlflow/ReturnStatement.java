package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import java.io.Serial;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Return statement, for returning from a function.
 */
public class ReturnStatement extends Statement implements JsonExportableObject {

    /**
     * Return control flow exception.
     */
    public static class Return extends JanitorControlFlowException {
        @Serial
        private static final long serialVersionUID = 1;
        private final JanitorObject value;

        public Return(final JanitorObject value) {
            this.value = value;
        }

        public JanitorObject getValue() {
            return value.janitorUnpack();
        }
    }

    /**
     * Return control flow exception, singleton instance for returning nothing ("void" does not exist, so we return NULL).
     */
    protected static final Return RETURN_NOTHING = new Return(JNull.NULL);

    private final Expression expression;

    /**
     * Constructor.
     * @param location where
     * @param expression what to return (optional)
     */
    public ReturnStatement(final Location location, final Expression expression) {
        super(location);
        this.expression = expression;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, Return {
        process.setCurrentLocation(getLocation());
        if (expression != null) {
            final JanitorObject returnValue = expression.evaluate(process).janitorUnpack();
            process.trace(() -> "return value: " + returnValue);
            throw new Return(returnValue);
        } else {
            throw RETURN_NOTHING;
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("expression", expression)
                .endObject();
    }

}
