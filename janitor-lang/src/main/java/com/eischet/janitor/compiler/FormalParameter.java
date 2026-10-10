package com.eischet.janitor.compiler;

import com.eischet.janitor.compiler.ast.expression.Expression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A parameter in the declaration of a function, as opposed to an argument in a call of it. */
public class FormalParameter {

    /** The kinds of parameters that a function can declare. */
    public enum Kind {
        POSITIONAL,
        DEFAULTED,
        VARARGS,
        KWARGS;

        /**
         * @return a human-readable name of this kind
         */
        public String title() {
            return switch (this) {
                case POSITIONAL -> "positional";
                case DEFAULTED -> "defaulted";
                case VARARGS -> "varargs";
                case KWARGS -> "kwargs";
            };
        }
    }

    private final @NotNull String name;
    private final @NotNull Kind kind;
    private final @Nullable Expression defaultValue;

    public FormalParameter(@NotNull final String name, @Nullable final Expression defaultValue, final @NotNull Kind kind) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.kind = kind;
    }

    /**
     * @return the name of the parameter
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * @return the kind of the parameter
     */
    public @NotNull Kind getKind() {
        return kind;
    }

    /**
     * @return the expression that produces the default value, or null if there is none
     */
    public @Nullable Expression getDefaultValue() {
        return defaultValue;
    }

    /**
     * Creates a positional parameter without a default value.
     * @param name the name of the parameter
     * @return the parameter
     */
    public static FormalParameter nonDefault(@NotNull final String name) {
        return new FormalParameter(name, null, Kind.POSITIONAL);
    }

    /**
     * Creates a parameter with a default value, which is used when the caller does not supply the argument.
     * @param name the name of the parameter
     * @param defaultValue the expression that produces the default value
     * @return the parameter
     */
    public static FormalParameter defaulted(@NotNull final String name, @NotNull final Expression defaultValue) {
        return new FormalParameter(name, defaultValue, Kind.DEFAULTED);
    }

    /**
     * Creates a parameter that collects all surplus positional arguments.
     * @param name the name of the parameter
     * @return the parameter
     */
    public static FormalParameter varargs(@NotNull final String name) {
        return new FormalParameter(name, null, Kind.VARARGS);
    }

    /**
     * Creates a parameter that collects all surplus keyword arguments.
     * @param name the name of the parameter
     * @return the parameter
     */
    public static FormalParameter kwargs(@NotNull final String name) {
        return new FormalParameter(name, null, Kind.KWARGS);
    }

    @Override
    public String toString() {
        return switch (kind) {
            case POSITIONAL -> name;
            case DEFAULTED -> name + " = " + defaultValue;
            case VARARGS -> "*" + name;
            case KWARGS -> "**" + name;
        };
    }

    /**
     * @return true if callers must always supply an argument for this parameter, i.e. it is positional
     */
    public boolean isMinimallyRequired() {
        return kind == Kind.POSITIONAL;
    }



}
