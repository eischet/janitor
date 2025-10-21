package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Postfix increment operator: i++;
 */
public class PostfixIncrement extends Statement implements Expression, JsonExportableObject {
    private final Expression expr;

    /**
     * Constructor.
     *
     * @param location where
     * @param expr     expression
     */
    public PostfixIncrement(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        process.trace(() -> "postfix increment: expr=" + expr);
        if (expr instanceof Identifier) {
            final String id = ((Identifier) expr).getText();
            final ResultAndScope scoped = process.lookupScopedVar(id);
            if (scoped == null) {
                throw new JanitorArgumentException(process, "variable not bound: '" + id + "'. cannot apply postfix++ to it.");
            }
            final JanitorObject currentValue = scoped.getVariable(); // oder das?? final Variable currentValue = expr.evaluate(runningScript);
            final JanitorObject newValue = JanitorSemantics.increment(process, currentValue);
            scoped.getScope().bind(process, id, newValue);  // vorher falsch: runningScript.getCurrentScope().bind(id, newValue);
            return currentValue;
        } else {
            throw new JanitorArgumentException(process, "cannot apply postfix++ to " + expr);
        }
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        evaluate(process); // just pass it on
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("expression", expr)
                .endObject();
    }

}
