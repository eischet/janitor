package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;

/**
 * Try-catch-finally statement: try { ... } catch (e) { ... } finally { ... }.
 * Having no way of mentioning types (like Java or Python), there cannot be more than one catch block here!
 */
public class TryCatchFinally extends Statement {

    private final Block tryBlock;
    private final String catchBind;
    private final Block catchBlock;
    private final Block finallyBlock;

    /**
     * Constructor.
     * @param location where
     * @param tryBlock try block
     * @param catchBind catch block binding "(e)"
     * @param catchBlock catch block
     * @param finallyBlock finally block
     */
    public TryCatchFinally(final Location location, final Block tryBlock, final String catchBind, final Block catchBlock, final Block finallyBlock) {
        super(location);
        this.tryBlock = tryBlock;
        this.catchBind = catchBind;
        this.catchBlock = catchBlock;
        this.finallyBlock = finallyBlock;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        if (catchBlock != null) {
            try {
                tryBlock.execute(runningScript);
            } catch (JanitorRuntimeException e) {
                try {
                    runningScript.enterBlock(null);
                    runningScript.getCurrentScope().bind(runningScript, catchBind, e);
                    catchBlock.execute(runningScript);
                } finally {
                    runningScript.exitBlock();
                }
            }
            if (finallyBlock != null) {
                finallyBlock.execute(runningScript);
            }
        } else {
            JanitorRuntimeException error = null;
            try {
                tryBlock.execute(runningScript);
            } catch (JanitorRuntimeException e) {
                error = e;
            }
            finallyBlock.execute(runningScript);
            if (error != null) {
                throw error;
            }
        }
    }
}
