package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableList;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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

    /**
     * A pseudo statement that wraps any JCallable.
     */
    public static class CallbackWrapper extends Statement implements Expression {

        private final JCallable callback;
        private final @Unmodifiable List<JanitorObject> args;

        public CallbackWrapper(final Location location, final JCallable callback, @Unmodifiable final List<JanitorObject> args) {
            super(location);
            this.callback = callback;
            this.args = args;
        }

        @Override
        public void writeJson(final JsonOutputStream producer) throws JsonException {
            throw new JsonException("you cannot convert a callback function to JSON");
        }

        @Override
        public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
            evaluate(process);
        }

        @Override
        public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
            final JanitorObject result = callback.call(process, new JCallArgs("callback", process, args));
            process.setScriptResult(result);
            return result;
        }

    }

    public static Script wrapperForCallback(final JCallable callable, final List<JanitorObject> args) {
        return new Script(null, List.of(new CallbackWrapper(null, callable, List.copyOf(args))), null);
    }

}
