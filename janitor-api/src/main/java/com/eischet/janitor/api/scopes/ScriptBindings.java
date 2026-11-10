// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.scopes;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Declares the global variables that a kind of script gets from its host, once for running and for editing.
 * <p>
 * Each binding has a name, a HELP text, a type and a function that computes its value from a context of the host, e.g.
 * the job a script runs for. {@link #binder(Object)} binds all of them when a script starts:
 * {@code script.run(BINDINGS.binder(context))}. Editors read {@link #declarations()} to suggest the names and, through the
 * types, their members.
 * </p>
 * <p>
 * A declared name always exists: when its function returns null, the script sees {@code null}. Bindings can extend
 * others, e.g. those that every script gets; a binding of the same name replaces the inherited one. Hosts that bind
 * variables the old way, with {@code process.bind(...)}, keep working, and what they bind after the binder wins.
 * </p>
 *
 * @param <C> the context the values are computed from
 */
public final class ScriptBindings<C> {

    /**
     * A declared global variable.
     *
     * @param name the name in scripts
     * @param type its type, which tells editors its members, e.g. {@link ScriptType#STRING}, {@code ScriptType.of(dispatcher)} or a
     *             list of such objects; {@link ScriptType#ANY} if unknown
     * @param help what the variable is, for editors and documentation
     */
    public record Declaration(@NotNull String name, @NotNull ScriptType type, @NotNull String help) {

        /**
         * @param name       the name in scripts
         * @param typeName   the name of its type as editors show it, e.g. "string" or "ShopDaoCollection"
         * @param dispatcher the dispatcher of its values; null for the built-in types, which editors know by their type name
         * @param help       what the variable is
         */
        public Declaration(final @NotNull String name, final @NotNull String typeName, final @Nullable Dispatcher<?> dispatcher, final @NotNull String help) {
            this(name, dispatcher != null ? new ScriptType.Of(typeName, dispatcher) : new ScriptType.Builtin(typeName), help);
        }

        /**
         * @return the name of the type as editors show it, e.g. {@code string}, {@code ShopDaoCollection} or {@code list<Order>}
         */
        public @NotNull String typeName() {
            return type.describe();
        }

        /**
         * @return the dispatcher of the values, or null if the type has none, e.g. a built-in type or a list
         */
        public @Nullable Dispatcher<?> dispatcher() {
            return type instanceof ScriptType.Of of ? of.dispatcher() : null;
        }
    }

    private record Binding<C>(@NotNull Declaration declaration, @NotNull Function<? super C, ? extends @Nullable JanitorObject> value) {
    }

    private final @Nullable ScriptBindings<? super C> parent;
    private final Map<String, Binding<C>> bindings = new LinkedHashMap<>();

    private ScriptBindings(final @Nullable ScriptBindings<? super C> parent) {
        this.parent = parent;
    }

    /**
     * @param <C> the context the values are computed from
     * @return new, empty bindings
     */
    public static <C> @NotNull ScriptBindings<C> create() {
        return new ScriptBindings<>(null);
    }

    /**
     * @param parent the bindings to inherit, which may compute their values from a more general context
     * @param <C>    the context the values are computed from
     * @return new bindings with those of the parent
     */
    public static <C> @NotNull ScriptBindings<C> extending(final @NotNull ScriptBindings<? super C> parent) {
        return new ScriptBindings<>(parent);
    }

    /**
     * Declares a variable whose values have a dispatcher.
     *
     * @param name       the name in scripts
     * @param typeName   the name of its type as editors show it
     * @param dispatcher the dispatcher of its values
     * @param help       what the variable is
     * @param value      computes the value from the context; may return null
     * @return these bindings, for chained calls
     */
    public @NotNull ScriptBindings<C> declare(final @NotNull String name, final @NotNull String typeName, final @Nullable Dispatcher<?> dispatcher,
                                              final @NotNull String help, final @NotNull Function<? super C, ? extends @Nullable JanitorObject> value) {
        bindings.put(name, new Binding<>(new Declaration(name, typeName, dispatcher, help), value));
        return this;
    }

    /**
     * Declares a variable of any type, e.g. a list of objects: {@code ScriptType.listOf(ScriptType.of(Order.DISPATCH))}.
     *
     * @param name  the name in scripts
     * @param type  its type
     * @param help  what the variable is
     * @param value computes the value from the context; may return null
     * @return these bindings, for chained calls
     */
    public @NotNull ScriptBindings<C> declare(final @NotNull String name, final @NotNull ScriptType type, final @NotNull String help,
                                              final @NotNull Function<? super C, ? extends @Nullable JanitorObject> value) {
        bindings.put(name, new Binding<>(new Declaration(name, type, help), value));
        return this;
    }

    /**
     * Declares a variable of a built-in type, e.g. "string", "int" or "bool".
     *
     * @param name     the name in scripts
     * @param typeName the built-in type
     * @param help     what the variable is
     * @param value    computes the value from the context; may return null
     * @return these bindings, for chained calls
     */
    public @NotNull ScriptBindings<C> declare(final @NotNull String name, final @NotNull String typeName, final @NotNull String help,
                                              final @NotNull Function<? super C, ? extends @Nullable JanitorObject> value) {
        return declare(name, typeName, null, help, value);
    }

    /**
     * @return all declared variables, inherited ones first, each name once
     */
    public @NotNull @Unmodifiable List<Declaration> declarations() {
        final Map<String, Declaration> all = new LinkedHashMap<>();
        if (parent != null) {
            parent.declarations().forEach(d -> all.put(d.name(), d));
        }
        bindings.forEach((name, binding) -> all.put(name, binding.declaration()));
        return List.copyOf(all.values());
    }

    /**
     * Binds all declared variables, inherited ones first; a variable whose value is null is bound to {@code null}.
     *
     * @param process the script process, before it runs
     * @param context the context to compute the values from
     */
    public void bind(final @NotNull JanitorScriptProcess process, final C context) {
        final List<Binding<? super C>> all = new ArrayList<>();
        collect(all);
        final Map<String, Binding<? super C>> effective = new LinkedHashMap<>();
        all.forEach(binding -> effective.put(binding.declaration().name(), binding));
        for (final Binding<? super C> binding : effective.values()) {
            final JanitorObject value = binding.value().apply(context);
            process.bind(binding.declaration().name(), value == null ? Janitor.NULL : value);
        }
    }

    /**
     * @param context the context to compute the values from
     * @return a callback for {@code RunnableScript.run(...)} that binds all declared variables
     */
    public @NotNull Consumer<JanitorScriptProcess> binder(final C context) {
        return process -> bind(process, context);
    }

    // the bindings of the parents and these, in that order; a later one replaces an earlier one of the same name when binding
    @SuppressWarnings("unchecked")
    private void collect(final List<Binding<? super C>> into) {
        if (parent != null) {
            ((ScriptBindings<C>) parent).collect(into);
        }
        into.addAll(bindings.values());
    }
}
