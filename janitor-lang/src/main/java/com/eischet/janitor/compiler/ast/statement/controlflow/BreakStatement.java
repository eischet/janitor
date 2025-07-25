package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import java.io.Serial;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Break statement, for breaking out of loops.
 */
public class BreakStatement extends Statement implements JsonExportableObject {

    /**
     * The break control flow exception, singleton instance.
     */
    protected static final Break BREAK = new Break();

    /**
     * Constructor.
     *
     * @param location where
     */
    public BreakStatement(final Location location) {
        super(location);
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws Break {
        process.setCurrentLocation(getLocation());
        throw BREAK;
    }

    /**
     * Break control flow exception.
     * This is thrown when a break statement is executed.
     * It is caught by the loop statement that is being broken out of.
     */
    public static class Break extends JanitorControlFlowException {
        @Serial
        private static final long serialVersionUID = 1;
        private Break() {
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .endObject();
    }

}
