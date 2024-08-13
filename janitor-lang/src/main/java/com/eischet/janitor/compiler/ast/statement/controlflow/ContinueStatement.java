package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import java.io.Serial;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Continue statement, for continuing loops (skipping the rest of the loops body and starting the next iteration).
 */
public class ContinueStatement extends Statement implements JsonExportableObject {

    /**
     * The continue control flow exception, singleton instance.
     */
    protected static final Continue CONTINUE = new Continue();

    /**
     * Constructor.
     *
     * @param location where
     */
    public ContinueStatement(final Location location) {
        super(location);
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws Continue {
        process.setCurrentLocation(getLocation());
        throw CONTINUE;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .endObject();
    }

    /**
     * Continue control flow exception.
     */
    public static class Continue extends JanitorControlFlowException {
        @Serial
        private static final long serialVersionUID = 1;
        private Continue() {
        }
    }

}
