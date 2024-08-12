package com.eischet.janitor.compiler.ast.function;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JCallable;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.controlflow.Block;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Script function.
 * This implements both functions and lambdas, so these are essentially the same thing internally.
 */
public class ScriptFunction extends AstNode implements Expression, JanitorObject, JCallable {

    private static final Logger log = LoggerFactory.getLogger(ScriptFunction.class);

    private final String name;
    private final List<String> parameterNames;
    private final Block block;
    private Scope moduleScope;
    private Scope closureScope;

    /**
     * Constructor.
     *
     * @param location       where the function is defined
     * @param name           name of the function
     * @param parameterNames names of the parameters
     * @param block          inner code of the function
     */
    public ScriptFunction(final Location location, final String name, final List<String> parameterNames, final Block block) {
        super(location);
        this.name = name;
        this.parameterNames = parameterNames;
        this.block = block;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "Function";
    }

    /**
     * Get the module scope of the function.
     * TODO: why is this currently not used, and what would happen if it were?
     *
     * @param moduleScope
     */
    public void setModuleScope(final Scope moduleScope) {
        // System.out.println("function " + name + " received module scope " + moduleScope);
        this.moduleScope = moduleScope;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        if (log.isDebugEnabled()) {
            log.debug("**** eval called on script function: " + name + " in scope with dir: {}", runningScript.getCurrentScope().dir());
        }
        this.closureScope = runningScript.getCurrentScope().hold();
        // A function initially evaluates "to itself", so it can later be called, because this class here
        // implements both Expression and JCallable. This is a (small) design decision, not a hard requirement.
        return this;
    }

    @Override
    public String janitorToString() {
        return "function " + name + "(" + String.join(", ", parameterNames) + ")";
    }

    @Override
    public JanitorObject call(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(parameterNames.size());
            try {
                if (moduleScope != null) {
                    process.pushModuleScope(moduleScope);
                }
                if (getLocation() != null) {
                    process.enterBlock(getLocation().nested(name));
                } else {
                    process.enterBlock(null); // anonyme Blöcke NICHT in den Stacktrace packen
                }
                process.pushClosureScope(closureScope);
                for (int i = 0; i < parameterNames.size(); i++) {
                    process.getCurrentScope().bind(process, parameterNames.get(i), arguments.get(i).janitorUnpack());
                }
                block.execute(process);
            } finally {
                process.popClosureScope(closureScope);
                process.exitBlock();
                if (moduleScope != null) {
                    process.popModuleScope(moduleScope);
                }
            }
        } catch (ReturnStatement.Return e) {
            process.trace(() -> "Function returned " + e.getValue());
            return e.getValue().janitorUnpack();
        } catch (JanitorControlFlowException e) {
            throw new JanitorInternalException(process, "invalid control flow exception within function call", e);
        }
        return JNull.NULL;
    }

}
