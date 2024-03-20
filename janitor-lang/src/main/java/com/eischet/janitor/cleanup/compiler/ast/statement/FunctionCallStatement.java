package com.eischet.janitor.cleanup.compiler.ast.statement;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.expression.ExpressionList;
import com.eischet.janitor.cleanup.compiler.ast.statement.controlflow.JanitorControlFlowException;
import com.eischet.janitor.cleanup.runtime.types.JAssignable;
import com.eischet.janitor.cleanup.runtime.types.JCallArgs;
import com.eischet.janitor.cleanup.runtime.types.JCallable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import static com.eischet.janitor.cleanup.tools.ObjectUtilities.simpleClassNameOf;

public class FunctionCallStatement extends Statement implements Expression {
    private final String functionName;
    private final Expression onExpression;
    private final ExpressionList expressionList;

    public FunctionCallStatement(final Location location, final String functionName, final Expression onExpression, final ExpressionList expressionList) {
        super(location);
        this.functionName = functionName;
        this.onExpression = onExpression;
        this.expressionList = expressionList;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + functionName + "@" + getLocation();
        // return super.toString(); xxx;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        runningScript.trace(() -> "execute via evaluate in " + this);
        evaluate(runningScript); // just pass it on
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        runningScript.trace(() -> "evaluating " + this);
        runningScript.setCurrentLocation(getLocation());
        JanitorObject function = null;
        if (onExpression != null) {
            runningScript.trace(() -> "looking up function from expression");

            final JanitorObject object = onExpression.evaluate(runningScript);
            runningScript.trace(() -> "object = " + object);


            function = object.janitorGetAttribute(runningScript, functionName, true);

        } else if (functionName != null) {
            runningScript.trace(() -> "looking up function '" + functionName + "' from scopes");
            function = runningScript.lookup(functionName);

        }
        final JanitorObject finalFunction = function;
        runningScript.trace(() -> "1 trying to call function: " + finalFunction);


        final ImmutableList<JanitorObject> finishedArgs;
        if (expressionList != null) {
            final MutableList<JanitorObject> args = Lists.mutable.empty();
            for (int i = 0; i < expressionList.length(); i++) {
                args.add(expressionList.get(i).evaluate(runningScript).janitorUnpack());
            }
            finishedArgs = args.toImmutable();
        } else {
            finishedArgs = Lists.immutable.empty();
        }
        runningScript.trace(() -> "args: " + finishedArgs);
        final JCallArgs args = new JCallArgs(functionName, runningScript, finishedArgs);

        //for (int i = 0; i < expressionList.length(); i++) {
        //    argumentList.bind("#"+i, expressionList.get(i).evaluate(runningScript));
        //}
        if (function instanceof JCallable) {
            try {
                final JanitorObject result = ((JCallable) function).call(runningScript, args);
                runningScript.trace(() -> "function call result: " + result);
                // LATER: hier nochmal genau prüfen, wo/wann wir auspacken müssen!
                if (result instanceof JAssignable) {
                    return result;
                }
                return result.janitorUnpack();
            } catch (RuntimeException e) {
                throw new JanitorNativeException(runningScript, "function call failed for " + functionName + "(" + args + ")", e);
            }
        }
        throw new JanitorNameException(runningScript, "invalid callable: " + functionName + " (name: " + functionName + "): " + function + " [" +
            simpleClassNameOf(function) + "]");
    }

}
