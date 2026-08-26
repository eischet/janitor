package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Try-catch-finally statement: try { ... } catch (e) { ... } finally { ... }.
 * Having no way of mentioning types (like Java or Python), there cannot be more than one catch block here!
 */
public class TryCatchFinally extends Statement implements JsonExportableObject {

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
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            if (catchBlock != null) {
                try {
                    tryBlock.execute(process);
                } catch (RuntimeException runtimeException) {
                    throw new JanitorNativeException(process, runtimeException.getMessage(), runtimeException);
                } catch (JanitorRuntimeException e) {
                    try {
                        process.enterBlock(null);
                        process.getCurrentScope().bind(process, catchBind, e);
                        catchBlock.execute(process);
                    } finally {
                        process.exitBlock();
                    }
                }
            } else {
                try {
                    tryBlock.execute(process);
                } catch (RuntimeException runtimeException) {
                    throw new JanitorNativeException(process, runtimeException.getMessage(), runtimeException);
                }
            }
        } finally {
            // This must run on every way out of the try/catch above: normal completion, a JanitorRuntimeException
            // (caught above or not, e.g. thrown again from inside the catch block), and also a control flow
            // signal (return/break/continue), which is a checked Exception, not a RuntimeException, and would
            // otherwise sail right past a catch(RuntimeException)/catch(JanitorRuntimeException) here.
            //
            // This used to be handled with an explicit catch-into-a-variable-then-rethrow instead of a real
            // Java "finally" block. That only ever caught JanitorRuntimeException, so a return/break/continue
            // thrown from the try block would skip the finallyBlock.execute(...) call entirely -- e.g.
            // "try { return 1; } finally { cleanup(); }" never ran cleanup(). Wrapping everything in a plain
            // Java try/finally fixes this for every exception type at once: finally always runs, and whatever
            // was in flight (an exception, or a control-flow signal) keeps propagating afterward automatically,
            // with no explicit rethrow needed -- see FirstParserTestCase#tryFinallyRunsOnControlFlow for tests.
            if (finallyBlock != null) {
                finallyBlock.execute(process);
            }
        }
    }


    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("tryBlock", tryBlock)
                .optional("catchBind", catchBind)
                .optional("catchBlock", catchBlock)
                .optional("finallyBlock", finallyBlock)
                .endObject();
    }

}
