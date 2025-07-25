package com.eischet.janitor.compiler.ast.function;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.controlflow.Block;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Script function.
 * This implements both functions and lambdas, so these are essentially the same thing internally.
 */
public class ScriptFunction extends AstNode implements Expression, JanitorObject, JCallable, JsonExportableObject {

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
     * Set the module scope of the function.
     * TODO: why is this currently not used, and what would happen if it were?
     *
     * @param moduleScope set the module scope
     */
    public void setModuleScope(final Scope moduleScope) {
        // System.out.println("function " + name + " received module scope " + moduleScope);
        this.moduleScope = moduleScope;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        if (log.isDebugEnabled()) {
            log.debug("**** eval called on script function: " + name + " in scope with dir: {}", process.getCurrentScope().dir());
        }
        this.closureScope = process.getCurrentScope().capture();
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
                    /*
                    if (closureScope != null) {
                        // Bind this to the closure scope, too, so it can later be referenced.
                        // Yes, having this here is a sign that the scoping needs more work in general...
                        closureScope.bind(process, parameterNames.get(i), arguments.get(i).janitorUnpack());
                    }

                     */
                }
                block.executeFunctionCall(process);
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

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("name", name)
                .optional("block", block);
        producer.key("parameterNames").beginArray();
        for (String parameterName : parameterNames) {
            producer.value(parameterName);
        }
        producer.endArray();
        producer.endObject();
        // TODO: output the two scopes, too?!
    }

}
