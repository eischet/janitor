package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.dispatch.FlatProperty;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.ArgumentList;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Function lookup statement.
 * This implements both foo.bar() and foo?.bar(), where the latter is called "guarded" in the constructor.
 * The guarded variant does not perform the call in case the function is not found or the onExpression evaluates to NULL.
 */
public class FunctionLookup extends Statement implements Expression, JsonExportableObject {
    private final String functionName;
    private final Expression onExpression;
    private final ArgumentList expressionList;
    private final boolean guarded;


    /**
     * Constructor.
     *
     * @param location       where
     * @param functionName   name of the function
     * @param onExpression   preceding expression
     * @param expressionList list of arguments
     * @param guarded        whether to return null if the function is not found
     */
    public FunctionLookup(final Location location, final String functionName, final Expression onExpression, final ArgumentList expressionList, final boolean guarded) {
        super(location);
        this.functionName = functionName;
        this.onExpression = onExpression;
        this.expressionList = expressionList;
        this.guarded = guarded;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        evaluate(process); // just pass it on
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        JanitorObject function = null;

        process.trace(() -> String.format("this=%s; evaluate onExpression=%s, functionName=%s, guarded=%s", this, onExpression, functionName, guarded));

        if (onExpression != null) {
            process.trace(() -> "top");
            process.trace(() -> "looking up function from expression " + onExpression);
            final JanitorObject object = onExpression.evaluate(process);
            process.trace(() -> "back in the function lookup! object: " + object);
            if (object == null) {
                process.trace(() -> "object is null!");
            } else {
                process.trace(() -> "object = " + object + " [" + object.getClass().getSimpleName() + "], functionName=" + functionName);
                function = object.janitorGetAttribute(process, functionName, false);
            }

            // LATER: auch Listen-Properties etc. berücksichtigen!

            if ((function == null || function == JNull.NULL) && object instanceof FlatProperty cp) {
                final Object v = cp.getValue();
                // System.err.println("looked up calc prop: " + v + " for " + functionName + " on " + onExpression);
                if (v instanceof JanitorObject nestedObject) {
                    function = nestedObject.janitorGetAttribute(process, functionName, false);
                    // System.err.println("function from calc pop: " + function);
                }
            }

            if (function == null /* || function == CSNull.NULL) */) {
                process.trace(() -> "function not found; guarded=" + guarded);
                if (guarded || object instanceof JMap) {
                    return JNull.NULL;
                }
                throw new JanitorNameException(process, "function/method not found: " + functionName + "; on: " + object + "[" + simpleClassNameOf(object) + "] --> " + function);
            }


        } else if (functionName != null) {
            process.trace(() -> "bottom");
            process.trace(() -> "looking up function '" + functionName + "' from scopes");
            function = process.lookup(functionName);
        }

        process.trace(() -> "check-1");


        if (expressionList == null && function instanceof JConstant) {
            process.trace(() -> "the 'function' is a constant and there's no expr. list --> returning it");
            return function;
        }

        process.trace(() -> "check-2");

        if (expressionList == null && function != null) { // war: instanceof MangedObject, mal gucken ob das noch funktioniert...
            process.trace(() -> "the 'function' is a managed object and there's no expr. list --> returning it");
            return function;
        }

        final JanitorObject finalFunction = function;
        process.trace(() -> "2 trying to call function: " + finalFunction);


        //for (int i = 0; i < expressionList.length(); i++) {
        //    argumentList.bind("#"+i, expressionList.get(i).evaluate(runningScript));
        //}
        if (function instanceof JCallable) {
            final JanitorObject finalFunction1 = function;
            process.trace(() -> "function LOOKUP: returning the function " + finalFunction1);
            return function;
            //final Variable result = ((Callable) function).call(runningScript, finishedArgs);
            //runningScript.trace(() -> "function call result: " + result);
            //return result;
        }
        process.trace(() -> "check-3");

        if (function != null) {
            final JanitorObject finalFunction2 = function;
            process.trace(() -> "function LOOKUP: returning the variable " + finalFunction2);
            return function;
        }
        process.trace(() -> "check-4");

        throw new JanitorNameException(process, "invalid callable: " + functionName + " (name: " + functionName + ")");
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("name", functionName)
                .optional("expression", onExpression)
                .optional("expressionList", expressionList)
                .optional("guarded", guarded)
                .endObject();
    }

}
