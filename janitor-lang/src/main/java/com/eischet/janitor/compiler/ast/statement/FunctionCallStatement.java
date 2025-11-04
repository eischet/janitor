package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.ArgumentList;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;


/**
 * Function call statement.
 * Wraps a function call expression.
 * @see ExpressionStatement
 */
public class FunctionCallStatement extends Statement implements Expression, JsonExportableObject {
    private final String functionName;
    private final Expression onExpression;
    private final ArgumentList expressionList;

    /**
     * Constructor.
     * @param location where
     * @param functionName name of the function
     * @param onExpression preceding expression
     * @param expressionList list of arguments
     */
    public FunctionCallStatement(final Location location, final String functionName, final Expression onExpression, final ArgumentList expressionList) {
        super(location);
        this.functionName = functionName;
        this.onExpression = onExpression;
        this.expressionList = expressionList;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + functionName + "@" + getLocation();
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        process.trace(() -> "execute via evaluate in " + this);
        evaluate(process); // just pass it on
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.trace(() -> "evaluating " + this);
        process.setCurrentLocation(getLocation());
        JanitorObject function = null;
        if (onExpression != null) {
            process.trace(() -> "looking up function from expression");

            final JanitorObject object = onExpression.evaluate(process);
            process.trace(() -> "object = " + object);


            function = object.janitorGetAttribute(process, functionName, true);

        } else if (functionName != null) {
            process.trace(() -> "looking up function '" + functionName + "' from scopes");
            function = process.lookup(functionName);

        }
        final JanitorObject finalFunction = function;
        process.trace(() -> "1 trying to call function: " + finalFunction);

        final JCallArgs args = expressionList == null ? JCallArgs.empty(functionName, process) : expressionList.toCallArguments(functionName, process);

        //for (int i = 0; i < expressionList.length(); i++) {
        //    argumentList.bind("#"+i, expressionList.get(i).evaluate(process));
        //}
        if (function instanceof JCallable) {
            try {
                final JanitorObject result = ((JCallable) function).call(process, args);
                process.trace(() -> "function call result: " + result);

                if (result == null) {
                    process.warn("expected result != null from call of " + functionName + "(" + args + ")");
                }

                // LATER: hier nochmal genau prüfen, wo/wann wir auspacken müssen!
                if (result instanceof JAssignable) {
                    return result;
                }
                return result.janitorUnpack();
            } catch (RuntimeException e) {
                throw new JanitorNativeException(process, "function call failed for " + functionName + "(" + args + ")", e);
            }
        }
        throw new JanitorNameException(process, "invalid callable: " + functionName + " (name: " + functionName + "): " + function + " [" +
            simpleClassNameOf(function) + "]");
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("name", functionName)
                .optional("expression", onExpression)
                .optional("expressionList", expressionList)
                .endObject();
    }
}
