package com.eischet.janitor.cleanup.compiler.ast.statement.controlflow;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.statement.Statement;

public class ContinueStatement extends Statement {

    public static class Continue extends JanitorControlFlowException {
        private Continue() {
        }
    }

    protected static final Continue CONTINUE = new Continue();
    public ContinueStatement(final Location location) {
        super(location);
    }


    @Override
    public void execute(final JanitorScriptProcess runningScript) throws Continue {
        runningScript.setCurrentLocation(getLocation());
        throw CONTINUE;
    }

}
