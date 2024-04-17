package com.eischet.janitor.compiler.ast.function;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.traits.JCallable;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.controlflow.Block;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptFunction extends AstNode implements Expression, JanitorObject, JCallable {
    private final String name;
    private final List<String> parameterNames;
    private final Block block;
    private Scope moduleScope;

    @Override
    public @NotNull String janitorClassName() {
        return "function";
    }

    public ScriptFunction(final Location location, final String name, final List<String> parameterNames, final Block block) {
        super(location);
        this.name = name;
        this.parameterNames = parameterNames;
        this.block = block;
    }

    public void setModuleScope(final Scope moduleScope) {
        // System.out.println("function " + name + " received module scope " + moduleScope);
        this.moduleScope = moduleScope;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return this; // eine Funktion ist sie selbst. Ich wünschte, ich könnte das auch immer von mir behaupten. :-))
    }

    @Override
    public String janitorToString() {
        return "function " + name + "(" + parameterNames.stream().collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public JanitorObject call(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(parameterNames.size());
            try {
                if (moduleScope != null) {
                    runningScript.pushModuleScope(moduleScope);
                }
                if (getLocation() != null) {
                    runningScript.enterBlock(getLocation().nested(name));
                } else {
                    runningScript.enterBlock(null); // anonyme Blöcke NICHT in den Stacktrace packen
                }
                for (int i = 0; i < parameterNames.size(); i++) {
                    runningScript.getCurrentScope().bind(runningScript, parameterNames.get(i), arguments.get(i).janitorUnpack());
                }
                block.execute(runningScript);
            } finally {
                runningScript.exitBlock();
                if (moduleScope != null) {
                    runningScript.popModuleScope(moduleScope);
                }
            }
        } catch (ReturnStatement.Return e) {
            runningScript.trace(() -> "Function returned " + e.getValue());
            return e.getValue().janitorUnpack();
        } catch (JanitorControlFlowException e) {
            throw new JanitorInternalException(runningScript, "invalid control flow exception within function call", e);
        }
        return JNull.NULL;
    }

    /*
    public static class FunctionCallExpression extends Expression {
        private final String functionName;
        private final ExpressionList expressionList;

        public FunctionCallExpression(final Location location, final String functionName, final ExpressionList expressionList) {
            super(sourceLine, sourceColumn);
            this.functionName = functionName;
            this.expressionList = expressionList;
        }

        @Override
        public Variable evaluate(final RunningScript runningScript) throws CockpitScriptRuntimeException {
            final Variable function = runningScript.getCurrentScope().lookup(functionName);
            runningScript.trace(() -> "trying to call function: " + function);
            //for (int i = 0; i < expressionList.length(); i++) {
            //    argumentList.bind("#"+i, expressionList.get(i).evaluate(runningScript));
            //}
            if (function instanceof Callable) {
                ((Callable) function).call(runningScript, Lists.immutable.empty()); // TO DO: add args
            }
            throw new CockpitScriptRuntimeException(this, "invalid callable: " + functionName + " (name: " + functionName + ")");
        }
    }
*/
}
