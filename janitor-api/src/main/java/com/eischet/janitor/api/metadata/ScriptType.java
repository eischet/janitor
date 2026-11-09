// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.metadata;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type of a value as tools see it, e.g. an editor that suggests the members of an expression.
 * <p>
 * Janitor is dynamically typed; these types only describe what a method returns or expects
 * ({@link Janitor.MetaData#RETURNS}, {@link Janitor.MetaData#PARAMETERS}), so that tools can follow an expression like
 * {@code orders.select(o -> o.}. They are never checked when a script runs. A type that is not known is {@link #ANY}.
 * </p>
 */
public sealed interface ScriptType {

    /** Nothing is known about the value. */
    ScriptType ANY = new Builtin("any");
    /** A string. */
    ScriptType STRING = new Builtin("string");
    /** An integer. */
    ScriptType INT = new Builtin("int");
    /** A floating point number. */
    ScriptType FLOAT = new Builtin("float");
    /** A boolean. */
    ScriptType BOOL = new Builtin("bool");
    /** A date. */
    ScriptType DATE = new Builtin("date");
    /** A date and time. */
    ScriptType DATETIME = new Builtin("datetime");
    /** A duration. */
    ScriptType DURATION = new Builtin("duration");
    /** A map. */
    ScriptType MAP = new Builtin("map");
    /** A list of unknown elements. */
    ScriptType LIST = new ListOf(ANY);
    /**
     * The objects that the receiver of the method holds, e.g. the entities of a DAO: the dispatcher that the receiver's
     * dispatcher names in {@link Janitor.MetaData#ENTITY}. Needed where one method is registered for many receivers.
     */
    ScriptType ENTITY = new Entity();

    /**
     * Replaces {@link #ENTITY} with the objects of a receiver.
     *
     * @param receiver the dispatcher of the object the method is called on, may be null
     * @return the type with the receiver's objects, or {@link #ANY} in place of an entity the receiver does not name
     */
    @NotNull ScriptType resolve(@Nullable Dispatcher<?> receiver);

    /**
     * @return how the type is written for people, e.g. "list&lt;Order&gt;"
     */
    @NotNull String describe();

    /**
     * A built-in type, named like the language reference names it.
     *
     * @param name the name, e.g. "string"
     */
    record Builtin(@NotNull String name) implements ScriptType {
        @Override
        public @NotNull ScriptType resolve(final @Nullable Dispatcher<?> receiver) {
            return this;
        }

        @Override
        public @NotNull String describe() {
            return name;
        }
    }

    /**
     * An object with a dispatcher.
     *
     * @param name       the name shown to people, e.g. the class name
     * @param dispatcher the dispatcher of the objects
     */
    record Of(@NotNull String name, @NotNull Dispatcher<?> dispatcher) implements ScriptType {
        @Override
        public @NotNull ScriptType resolve(final @Nullable Dispatcher<?> receiver) {
            return this;
        }

        @Override
        public @NotNull String describe() {
            return name;
        }
    }

    /**
     * A list.
     *
     * @param element the type of the elements
     */
    record ListOf(@NotNull ScriptType element) implements ScriptType {
        @Override
        public @NotNull ScriptType resolve(final @Nullable Dispatcher<?> receiver) {
            return new ListOf(element.resolve(receiver));
        }

        @Override
        public @NotNull String describe() {
            return element == ANY ? "list" : "list<" + element.describe() + ">";
        }
    }

    /**
     * A function, e.g. a lambda passed to a method.
     *
     * @param parameters the types of its parameters
     * @param returns    what it returns
     */
    record Function(@NotNull List<ScriptType> parameters, @NotNull ScriptType returns) implements ScriptType {
        @Override
        public @NotNull ScriptType resolve(final @Nullable Dispatcher<?> receiver) {
            return new Function(parameters.stream().map(p -> p.resolve(receiver)).toList(), returns.resolve(receiver));
        }

        @Override
        public @NotNull String describe() {
            return "function(" + parameters.stream().map(ScriptType::describe).collect(Collectors.joining(", ")) + ") -> " + returns.describe();
        }
    }

    /**
     * The objects of the receiver, see {@link #ENTITY}.
     */
    record Entity() implements ScriptType {
        @Override
        public @NotNull ScriptType resolve(final @Nullable Dispatcher<?> receiver) {
            final Dispatcher<?> entity = receiver == null ? null : receiver.getMetaData(Janitor.MetaData.ENTITY);
            return entity == null ? ANY : of(entity);
        }

        @Override
        public @NotNull String describe() {
            return "entity";
        }
    }

    /**
     * A parameter of a method.
     *
     * @param name the name, as in the call form of the HELP text
     * @param type its type
     */
    record Parameter(@NotNull String name, @NotNull ScriptType type) {
    }

    /**
     * @param dispatcher a dispatcher
     * @return the type of its objects, named by its {@link Janitor.MetaData#CLASS} metadata
     */
    static @NotNull ScriptType of(final @NotNull Dispatcher<?> dispatcher) {
        final String name = dispatcher.getMetaData(Janitor.MetaData.CLASS);
        return new Of(name == null ? "object" : name, dispatcher);
    }

    /**
     * @param element the type of the elements
     * @return a list of them
     */
    static @NotNull ScriptType listOf(final @NotNull ScriptType element) {
        return new ListOf(element);
    }

    /**
     * @param returns    what the function returns
     * @param parameters the types of its parameters
     * @return the function type
     */
    static @NotNull ScriptType function(final @NotNull ScriptType returns, final @NotNull ScriptType... parameters) {
        return new Function(List.of(parameters), returns);
    }

    /**
     * @param parameters the parameters of a method
     * @return them as a list, for {@link Janitor.MetaData#PARAMETERS}
     */
    static @NotNull List<Parameter> parameters(final @NotNull Parameter... parameters) {
        return List.copyOf(Arrays.asList(parameters));
    }

    /**
     * @param name the name
     * @param type the type
     * @return the parameter
     */
    static @NotNull Parameter parameter(final @NotNull String name, final @NotNull ScriptType type) {
        return new Parameter(name, type);
    }
}
