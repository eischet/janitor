package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableList;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The root of all scripting.
 * This is the top-level script, containing all statements.
 */
public class Script extends Statement implements JsonExportableList {

    private final @Nullable String source;
    private final List<Statement> statements;

    /**
     * Constructor.
     *
     * @param location   where
     * @param statements list of statements
     * @param source     source code
     */
    public Script(final Location location, final List<Statement> statements, final @Nullable String source) {
        super(location);
        this.statements = statements;
        this.source = source;
    }

    /**
     * Get the source code.
     *
     * @return source code
     */
    public @Nullable String getSource() {
        return source;
    }

    /**
     * Get the list of statements.
     *
     * @return list of statements
     */
    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        process.trace(() -> "executing " + getStatements().size() + " statements at top level...");
        for (final Statement statement : getStatements()) {
            process.trace(() -> "executing: " + statement);
            statement.execute(process);
        }
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return false;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginArray();
        for (Statement statement : statements) {
            statement.writeJson(producer);
        }
        producer.endArray();
    }

}
