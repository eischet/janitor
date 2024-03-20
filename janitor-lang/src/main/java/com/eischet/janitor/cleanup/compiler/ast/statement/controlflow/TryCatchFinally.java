package com.eischet.janitor.cleanup.compiler.ast.statement.controlflow;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.statement.Statement;

public class TryCatchFinally extends Statement {

    private final Block tryBlock;
    private final String catchBind;
    private final Block catchBlock;
    private final Block finallyBlock;

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
                    runningScript.getCurrentScope().bind(catchBind, e);
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
