// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.scopes;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * A scope answers which names it defines itself; the interpreter walks the chain of parents, so a scope never looks
 * further than its own names.
 * <p>
 * There are two kinds: a {@link VariableScope} holds variables, e.g. those of a script, a block, a module or the
 * environment's built-in scope; an {@link ObjectScope} offers the attributes of an object as names, e.g. the fields of the
 * object a filter script checks, or the values of a template. Hosts put object scopes between a script and the built-in
 * scope ({@code RunnableScript.runInScope}).
 * </p>
 * <p>
 * Binding and assigning are different things: the interpreter binds parameters, loop and catch variables, imports and new
 * variables in the scope it runs in, which shadows names from outer scopes; an assignment to a name that an outer scope
 * already defines goes to that scope ({@link #assign}), which decides what that means.
 * </p>
 */
public interface Scope extends JanitorObject, HasNameDocs {

    /**
     * @return the scope that is asked next when this one does not know a name, or null at the end of the chain
     */
    @Nullable Scope getParent();

    /**
     * Looks up a name in this scope only; no parent, module or closure scope is consulted.
     *
     * @param process the running script
     * @param name    the name
     * @return its value, or null if this scope does not define the name
     */
    @Nullable JanitorObject get(@NotNull JanitorScriptProcess process, @NotNull String name);

    /**
     * Assigns a value to a name that this scope defines, as in {@code name = value}. A scope that does not hold variables
     * of its own, or must not be changed by scripts, shadows the name in the script's main scope instead.
     *
     * @param process the running script
     * @param name    the name
     * @param value   the new value
     * @throws JanitorRuntimeException if the value cannot be assigned
     */
    void assign(@NotNull JanitorScriptProcess process, @NotNull String name, @NotNull JanitorObject value) throws JanitorRuntimeException;

    /**
     * @return the names this scope defines itself, for tools like {@code dir} and editors
     */
    @NotNull @Unmodifiable Collection<String> names();

    @Override
    default @NotNull String janitorClassName() {
        return "scope";
    }

}
