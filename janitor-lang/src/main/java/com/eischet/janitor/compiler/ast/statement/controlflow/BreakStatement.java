package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;

public class BreakStatement extends Statement {

    public static class Break extends JanitorControlFlowException {
        private Break() {
        }
    }

    protected static final Break BREAK = new Break();

    public BreakStatement(final Location location) {
        super(location);
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws Break {
        runningScript.setCurrentLocation(getLocation());
        throw BREAK;
    }

}
