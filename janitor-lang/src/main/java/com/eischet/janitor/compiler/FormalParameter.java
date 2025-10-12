package com.eischet.janitor.compiler;

import com.eischet.janitor.compiler.ast.expression.Expression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormalParameter {

    public enum Kind {
        POSITIONAL,
        DEFAULTED,
        VARARGS,
        KWARGS;

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

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Kind getKind() {
        return kind;
    }

    public @Nullable Expression getDefaultValue() {
        return defaultValue;
    }

    public static FormalParameter nonDefault(@NotNull final String name) {
        return new FormalParameter(name, null, Kind.POSITIONAL);
    }

    public static FormalParameter defaulted(@NotNull final String name, @NotNull final Expression defaultValue) {
        return new FormalParameter(name, defaultValue, Kind.DEFAULTED);
    }

    public static FormalParameter varargs(@NotNull final String name) {
        return new FormalParameter(name, null, Kind.VARARGS);
    }

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

    public boolean isMinimallyRequired() {
        return kind == Kind.POSITIONAL;
    }



}
