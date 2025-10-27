package com.eischet.janitor.compiler.ast.function;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.compiler.FormalParameter;
import com.eischet.janitor.compiler.FormalParameters;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.ArgumentList;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.controlflow.Block;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Script function.
 * This implements both functions and lambdas, so these are essentially the same thing internally.
 */
public class ScriptFunction extends AstNode implements Expression, JanitorObject, JCallable, JsonExportableObject {

    private static final Logger log = LoggerFactory.getLogger(ScriptFunction.class);

    private final String name;
    private final FormalParameters formalParameters;
    private final Block block;
    private Scope moduleScope;
    private Scope closureScope;

    /**
     * Constructor.
     *
     * @param location         where the function is defined
     * @param name             name of the function
     * @param formalParameters names of the parameters
     * @param block            inner code of the function
     */
    public ScriptFunction(final Location location, final String name, final FormalParameters formalParameters, final Block block) {
        super(location);
        this.name = name;
        this.formalParameters = formalParameters;
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
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
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
        return "function " + name + "(" + formalParameters + ")";
    }

    @Override
    public JanitorObject call(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.requireAtLeast(formalParameters.minSize());
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
                final int nonDefaultSize = formalParameters.minSize();

                if (!formalParameters.getParameters().isEmpty()) {
                    // start at the first argument:
                    int argPos = 0;
                    // go though all parameters that the function needs:

                    // TODO: do not let these go into kwargs, or don't care, have to decide what's better: final Set<String> usedNames = new HashSet<>();

                    for (final FormalParameter parameter : formalParameters) {
                        if (parameter.getKind() == FormalParameter.Kind.POSITIONAL) {
                            final JanitorObject matched = arguments.get(argPos++).janitorUnpack();
                            process.getCurrentScope().bind(process, parameter.getName(), matched);
                        } else if (parameter.getKind() == FormalParameter.Kind.VARARGS) {
                            @NotNull final JList list = Janitor.list();
                            while (argPos < arguments.size()) {
                                final JanitorObject matched = arguments.get(argPos++).janitorUnpack();
                                list.add(matched);
                            }
                            process.getCurrentScope().bind(process, parameter.getName(), list);
                        } else if (parameter.getKind() == FormalParameter.Kind.DEFAULTED) {
                            // these are a bit tricky, because (stealing the idea from Python without thinking it through first ;-) )
                            // these can be positional or named, but not both.

                            // Get the possible named argument, but don't use it yet:
                            JanitorObject valueByName = arguments.getByName(parameter.getName());

                            // First, check if there is a named argument matching our name
                            JanitorObject valueByPosition = null;
                            if (argPos < arguments.size()) {
                                valueByPosition = arguments.get(argPos++).janitorUnpack();
                            }

                            JanitorObject defaultValue = parameter.getDefaultValue().evaluate(process).janitorUnpack();

                            if (valueByName == null && valueByPosition == null) {
                                // Simple: neither matching position nor name is found, use the default value.
                                // System.out.println("using default value for parameter " + parameter.getName());
                                process.getCurrentScope().bind(process, parameter.getName(), defaultValue);
                            } else if (valueByName == null && valueByPosition != null) {
                                // Simple, too: only a matching positional argument is found, use that
                                process.getCurrentScope().bind(process, parameter.getName(), valueByPosition);
                            } else if (valueByName != null && valueByPosition != null && valueByName == valueByPosition) {
                                // It's the same thing, no need to worry
                                process.getCurrentScope().bind(process, parameter.getName(), valueByPosition);
                            } else {
                                System.err.println("don't know what to do: valueByName=" + valueByName + ", valueByPosition=" + valueByPosition);
                                // TODO: there are missing cases!
                                throw new JanitorArgumentException(process, "Default parameters are not yet implemented");
                            }
                        } else if (parameter.getKind() == FormalParameter.Kind.KWARGS) {
                            // my first impulse was to keep all "used" args out of the map, but it cannot hurt us at all to use them, too
                            @NotNull final JMap kwargs = arguments.asKwargs(null);
                            process.getCurrentScope().bind(process, parameter.getName(), kwargs);
                            // throw new JanitorArgumentException(process, "kwargs parameters are not yet implemented");
                        }
                    }
                }


                /*

                for (int i = 0; i < nonDefaultSize; i++) {
                    process.getCurrentScope().bind(process, formalParameters.get(i).getName(), arguments.get(i).janitorUnpack());
                    //if (closureScope != null) {
                    //    // Bind this to the closure scope, too, so it can later be referenced.
                    //    // Yes, having this here is a sign that the scoping needs more work in general...
                    //    closureScope.bind(process, parameterNames.get(i), arguments.get(i).janitorUnpack());
                    //}
                // TODO: bind additional parameters to *args, if that is available
                if (formalParameters.size() > nonDefaultSize) {

                }


                // TODO: bind **kwargs, as soon as they are in the Grammar for calling

                }
                     */


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
        for (var parameterName : formalParameters) {
            producer.value(parameterName.toString());
        }
        producer.endArray();
        producer.endObject();
        // TODO: output the two scopes, too?!
    }

}
