package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;

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
