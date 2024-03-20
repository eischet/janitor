package com.eischet.janitor.cleanup.compiler.ast.statement;

import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.expression.ExpressionList;
import com.eischet.janitor.cleanup.compiler.ast.statement.controlflow.JanitorControlFlowException;
import com.eischet.janitor.cleanup.tools.Interner;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JConstant;
import com.eischet.janitor.cleanup.api.api.types.JNull;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.runtime.types.FlatProperty;
import com.eischet.janitor.cleanup.runtime.types.JCallable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import static com.eischet.janitor.cleanup.tools.ObjectUtilities.simpleClassNameOf;

public class FunctionLookup extends Statement implements Expression {
    private final String functionName;
    private final Expression onExpression;
    private final ExpressionList expressionList;
    private final boolean guarded;

    public FunctionLookup(final Location location, final String functionName, final Expression onExpression, final ExpressionList expressionList, final boolean guarded) {
        super(location);
        this.functionName = Interner.maybeIntern(functionName);
        this.onExpression = onExpression;
        this.expressionList = expressionList;
        this.guarded = guarded;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        evaluate(runningScript); // just pass it on
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        runningScript.setCurrentLocation(getLocation());
        JanitorObject function = null;
        if (onExpression != null) {
            runningScript.trace(() -> "looking up function from expression " + onExpression);
            final JanitorObject object = onExpression.evaluate(runningScript);
            if (object == null) {
                runningScript.trace(() -> "object is null!");
            } else {
                runningScript.trace(() -> "object = " + object + " [" + object.getClass().getSimpleName() + "], functionName=" + functionName);
                function = object.janitorGetAttribute(runningScript, functionName, false);
            }

            // LATER: auch Listen-Properties etc. berücksichtigen!

            if ((function == null || function == JNull.NULL) && object instanceof FlatProperty cp) {
                final Object v = cp.getValue();
                // System.err.println("looked up calc prop: " + v + " for " + functionName + " on " + onExpression);
                if (v instanceof JanitorObject nestedObject) {
                    function = nestedObject.janitorGetAttribute(runningScript, functionName, false);
                    // System.err.println("function from calc pop: " + function);
                }
            }

            if (function == null /* || function == CSNull.NULL) */) {
                if (guarded) {
                    return JNull.NULL;
                }
                throw new JanitorNameException(runningScript,
                    "function/method not found: " + functionName + "; on: " + object + "[" + simpleClassNameOf(object) + "] --> " + function);
            }


        } else if (functionName != null) {
            runningScript.trace(() -> "looking up function '" + functionName + "' from scopes");
            function = runningScript.lookup(functionName);
        }

        if (expressionList == null && function instanceof JConstant) {
            runningScript.trace(() -> "the 'function' is a constant and there's no expr. list --> returning it");
            return function;
        }
        if (expressionList == null && function != null) { // war: instanceof MangedObject, mal gucken ob das noch funktioniert...
            runningScript.trace(() -> "the 'function' is a managed object and there's no expr. list --> returning it");
            return function;
        }

        final JanitorObject finalFunction = function;
        runningScript.trace(() -> "2 trying to call function: " + finalFunction);


        final ImmutableList<JanitorObject> finishedArgs;
        if (expressionList != null) {
            final MutableList<JanitorObject> args = Lists.mutable.empty();
            for (int i = 0; i < expressionList.length(); i++) {
                args.add(expressionList.get(i).evaluate(runningScript));
            }
            finishedArgs = args.toImmutable();
        } else {
            finishedArgs = Lists.immutable.empty();
        }
        runningScript.trace(() -> "args: " + finishedArgs);

        //for (int i = 0; i < expressionList.length(); i++) {
        //    argumentList.bind("#"+i, expressionList.get(i).evaluate(runningScript));
        //}
        if (function instanceof JCallable) {
            final JanitorObject finalFunction1 = function;
            runningScript.trace(() -> "function LOOKUP: returning the function " + finalFunction1);
            return function;
            //final Variable result = ((Callable) function).call(runningScript, finishedArgs);
            //runningScript.trace(() -> "function call result: " + result);
            //return result;
        }

        if (function != null) {
            final JanitorObject finalFunction2 = function;
            runningScript.trace(() -> "function LOOKUP: returning the variable " + finalFunction2);
            return function;
        }

        throw new JanitorNameException(runningScript, "invalid callable: " + functionName + " (name: " + functionName + ")");
    }

}
