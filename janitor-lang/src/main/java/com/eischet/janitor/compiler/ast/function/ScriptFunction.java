// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.function;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorInternalException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.ModuleScopeAware;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.tree.ConstantNode;
import com.eischet.janitor.api.types.functions.tree.ExpressionNode;
import com.eischet.janitor.api.types.functions.tree.JInspectableFunction;
import com.eischet.janitor.compiler.FormalParameter;
import com.eischet.janitor.compiler.FormalParameters;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.controlflow.Block;
import com.eischet.janitor.compiler.ast.statement.controlflow.ReturnStatement;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Script function.
 * This implements both functions and lambdas, so these are essentially the same thing internally.
 * <p>
 * This class itself is an AST node: it represents the source-level function/lambda literal, and is
 * created exactly once per literal at compile time. It is therefore shared by every evaluation of
 * that literal (e.g. every call to an outer function that contains this literal). Because of that,
 * this node must NOT carry any per-evaluation state (like a captured scope) itself -- doing so used
 * to make every closure created from the same literal alias the same captured scope, i.e. the very
 * last one captured, no matter which one was actually invoked.
 * </p>
 * <p>
 * Instead, {@link #evaluate(JanitorScriptProcess)} produces a fresh {@link Closure} value every time
 * it runs, each with its own captured scope, while still sharing the (immutable) code -- name,
 * parameters, and body -- held by this node.
 * </p>
 */
public class ScriptFunction extends AstNode implements Expression, JsonExportableObject, ModuleScopeAware {

    private static final JanitorLogger log = JanitorLogger.getLogger(ScriptFunction.class);

    private final String name;
    private final FormalParameters formalParameters;
    private final Block block;
    private Scope moduleScope;
    private ExpressionTreeBuilder.Result expressionTree; // built on first request, see expressionTree()
    private boolean expressionTreeBuilt;

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
    public void setModuleScope(final Scope moduleScope) {
        // System.out.println("function " + name + " received module scope " + moduleScope);
        this.moduleScope = moduleScope;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        if (log.isDebugEnabled()) {
            log.debug("**** eval called on script function: " + name + " in scope with dir: {}", process.getCurrentScope().names());
        }
        // A function/lambda literal evaluates to a fresh closure every time, capturing whichever
        // scope is active right now. This closure "initially evaluates to itself", so it can later
        // be called, because Closure implements both JanitorObject and JCallable. This is a (small)
        // design decision, not a hard requirement.
        return new Closure(process.getCurrentScope());
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

    /**
     * Returns the tree of the expression this function returns, if its body is a single return statement, which is
     * always the case for expression lambdas like "x -> x * 2". The tree belongs to the code, not to a closure, so it
     * is built once and shared by all closures of this literal.
     *
     * @return the tree, or null if the body is anything else
     */
    private synchronized @Nullable ExpressionTreeBuilder.Result expressionTree() {
        if (!expressionTreeBuilt) {
            if (block.getStatements().size() == 1
                && block.getStatements().getFirst() instanceof ReturnStatement returnStatement
                && returnStatement.getExpression() != null) {
                expressionTree = ExpressionTreeBuilder.build(returnStatement.getExpression(), parameterNames());
            }
            expressionTreeBuilt = true;
        }
        return expressionTree;
    }

    private @NotNull @Unmodifiable List<String> parameterNames() {
        return formalParameters.getParameters().stream().map(FormalParameter::getName).toList();
    }

    /**
     * One specific closure value produced by evaluating this function/lambda literal: the shared
     * code from the enclosing ScriptFunction, plus the scope that was active at the moment this
     * particular closure was created. Every evaluation of the same literal produces its own Closure
     * instance, so closures created at different times (e.g. by separate calls to an outer function)
     * no longer alias each other's captured scope.
     */
    public final class Closure implements JanitorObject, JInspectableFunction {

        private final Scope closureScope;

        private Closure(final Scope closureScope) {
            this.closureScope = closureScope;
        }

        @Override
        public @NotNull @Unmodifiable List<String> getParameterNames() {
            return parameterNames();
        }

        @Override
        public @NotNull Optional<ExpressionNode> getExpressionTree() {
            return Optional.ofNullable(expressionTree()).map(ExpressionTreeBuilder.Result::root);
        }

        @Override
        public @NotNull JanitorObject evaluate(final @NotNull JanitorScriptProcess process, final @NotNull ExpressionNode node) throws JanitorRuntimeException {
            final ExpressionTreeBuilder.Result tree = expressionTree();
            final Expression expression = tree == null ? null : tree.sources().get(node);
            if (expression == null) {
                throw new IllegalArgumentException("the node does not belong to the expression tree of " + janitorToString() + ": " + node);
            }
            if (node.usesParameters()) {
                throw new IllegalArgumentException("the node uses the parameters of " + janitorToString() + ": " + node);
            }
            if (node instanceof ConstantNode constant) {
                return constant.value();
            }
            // Same scopes as in call(), but without binding any parameters, which the node does not use anyway.
            if (moduleScope != null) {
                process.pushModuleScope(moduleScope);
            }
            process.enterBlock(null);
            process.pushClosureScope(closureScope);
            try {
                return expression.evaluate(process).janitorUnpack();
            } finally {
                process.popClosureScope(closureScope);
                process.exitBlock();
                if (moduleScope != null) {
                    process.popModuleScope(moduleScope);
                }
            }
        }

        @Override
        public @NotNull String janitorClassName() {
            return "Function";
        }

        @Override
        public @NotNull String janitorToString() {
            return "function " + name + "(" + formalParameters + ")";
        }

        @Override
        public JanitorObject call(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
            process.countInstruction();
            try {
                // Arguments are matched before the function's scope exists, so that a wrong call fails at the caller.
                final JanitorObject[] values = formalParameters.layout().match(process, arguments);
                try {
                    if (moduleScope != null) {
                        process.pushModuleScope(moduleScope);
                    }
                    if (getLocation() != null) {
                        process.enterBlock(getLocation().nested(name));
                    } else {
                        process.enterBlock(null); // do NOT put anonymous blocks into the stack trace
                    }
                    process.pushClosureScope(closureScope);
                    // Bound in the order of the parameters, so that a default value can use the parameters before it.
                    final List<FormalParameter> parameters = formalParameters.getParameters();
                    for (int i = 0; i < values.length; i++) {
                        final FormalParameter parameter = parameters.get(i);
                        final JanitorObject value = values[i] != null ? values[i] : parameter.getDefaultValue().evaluate(process).janitorUnpack();
                        process.getCurrentScope().bind(process, parameter.getName(), value);
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

    }

}
