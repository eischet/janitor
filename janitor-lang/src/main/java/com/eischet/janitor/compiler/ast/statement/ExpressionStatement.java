package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * An expression statement.
 * <p>This wraps an expression, which cannot be executed as a statement, to make it executable anyway.</p>
 * <p>Example: foo(); is represented an expression statement that wraps the expression foo().<br>
 * Counter example: bar = foo(); is an assignment statement that contains the expression foo().</p>
 * <p>Note: there is no "StatementExpression" for the inverse case. That's why you cannot write: if (foo = bar) { ... }.
 * foo = bar is an assignment statement, but if() requires an expression to work.</p>
 * TODO: I'm not sure how this currently relates to the FunctionCallStatement that's used in some places.
 * @see FunctionCallStatement
 */
public class ExpressionStatement extends Statement implements JsonExportableObject {

    private final Expression expression;

    /**
     * Constructor.
     *
     * @param location   where
     * @param expression expression
     */
    public ExpressionStatement(final Location location, final Expression expression) {
        super(location);
        this.expression = expression;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        process.setCurrentLocation(getLocation());
        process.setScriptResult(expression.evaluate(process).janitorUnpack());
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("expression", expression)
                .endObject();
    }
}
