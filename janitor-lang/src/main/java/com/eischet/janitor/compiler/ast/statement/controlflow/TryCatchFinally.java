package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
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
        if (catchBlock != null) {
            try {
                tryBlock.execute(process);
            } catch (JanitorRuntimeException e) {
                try {
                    process.enterBlock(null);
                    process.getCurrentScope().bind(process, catchBind, e);
                    catchBlock.execute(process);
                } finally {
                    process.exitBlock();
                }
            }
            if (finallyBlock != null) {
                finallyBlock.execute(process);
            }
        } else {
            JanitorRuntimeException error = null;
            try {
                tryBlock.execute(process);
            } catch (JanitorRuntimeException e) {
                error = e;
            }
            finallyBlock.execute(process);
            if (error != null) {
                throw error;
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
