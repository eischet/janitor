// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
 * A script function or lambda whose code the host can look at, e.g. to turn {@code item -> item.code == "FOO"} into a
 * database query instead of calling it for every item.
 * <p>
 * Script functions and lambdas implement this in addition to {@link JCallable}, so a host method that receives one can
 * check for this interface and fall back to calling it otherwise. Calling it works the same either way.
 * </p>
 */
public interface JInspectableFunction extends JCallable {

    /**
     * @return the names of the parameters, in the order of the declaration
     */
    @NotNull @Unmodifiable List<String> getParameterNames();

    /**
     * Returns the expression this function returns, as a tree. That is available for lambdas like {@code x -> x * 2}
     * and for functions whose body consists of a single {@code return} statement, and empty for all others.
     *
     * @return the expression tree, if the function consists of a single expression
     */
    @NotNull Optional<ExpressionNode> getExpressionTree();

    /**
     * Evaluates a part of the {@link #getExpressionTree() expression tree} that does not use the parameters, e.g. a
     * captured variable like {@code code} in {@code item -> item.code == code}, in the scope the function captured.
     *
     * @param process the running script
     * @param node a node of this function's expression tree, for which {@link ExpressionNode#usesParameters()} is false
     * @return the value
     * @throws JanitorRuntimeException if the evaluation failed
     * @throws IllegalArgumentException if the node uses parameters or does not belong to this function's tree
     */
    @NotNull JanitorObject evaluate(@NotNull JanitorScriptProcess process, @NotNull ExpressionNode node) throws JanitorRuntimeException;

}
