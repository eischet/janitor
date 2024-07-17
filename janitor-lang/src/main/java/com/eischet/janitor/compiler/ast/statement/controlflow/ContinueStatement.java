package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;

import java.io.Serial;

/**
 * Continue statement, for continuing loops (skipping the rest of the loops body and starting the next iteration).
 */
public class ContinueStatement extends Statement {

    /**
     * The continue control flow exception, singleton instance.
     */
    protected static final Continue CONTINUE = new Continue();

    /**
     * Constructor.
     *
     * @param location
     */
    public ContinueStatement(final Location location) {
        super(location);
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws Continue {
        runningScript.setCurrentLocation(getLocation());
        throw CONTINUE;
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
