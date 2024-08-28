package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableList;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import java.util.List;

/**
 * Block of statements.
 * That's really just a simple list of other statements, dressed as a statement.
 */
public class Block extends Statement implements JsonExportableList {

    private final List<Statement> statements;

    /**
     * Constructor.
     *
     * @param location   where
     * @param statements the statements
     */
    public Block(final Location location, final List<Statement> statements) {
        super(location);
        this.statements = statements;
    }

    /**
     * Get the statements.
     *
     * @return the statements
     */
    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            process.setCurrentLocation(getLocation());
            if (getLocation() != null) {
                process.enterBlock(null);
            }
            process.trace(() -> "executing " + getStatements().size() + " statements...");
            for (final Statement statement : getStatements()) {
                process.trace(() -> "executing: " + statement);
                statement.execute(process);
            }
        } finally {
            process.exitBlock();
        }
    }

    /**
     * Execute the block as part of a function call, which means that the caller manages variables' nesting etc.!
     *
     * @param process the current script process
     * @throws JanitorRuntimeException on errors
     * @throws JanitorControlFlowException on errors
     */
    public void executeFunctionCall(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        for (final Statement statement : getStatements()) {
            statement.execute(process);
        }
    }


    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginArray();
        for (final Statement statement : getStatements()) {
            statement.writeJson(producer);
        }
        producer.endArray();
    }
}
