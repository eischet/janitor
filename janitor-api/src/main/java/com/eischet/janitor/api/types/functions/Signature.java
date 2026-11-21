// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes a native method once, for both people and the runtime: its parameters, their types and default values, what it
 * returns, and what it does. From this, the dispatch table sets the HELP text (with the call form, e.g.
 * {@code read(path, charset = "UTF-8"): ...}) and the {@code PARAMETERS} and {@code RETURNS} metadata, and binds the
 * arguments of each call by the same rules as script functions ({@link ParameterLayout}), before the method runs.
 *
 * <pre>{@code
 * static final Signature READ = Signature.method("read")
 *         .param("path", ScriptType.STRING)
 *         .optional("charset", ScriptType.STRING, "UTF-8")
 *         .returns(ScriptType.STRING)
 *         .help("Returns the contents of a file as text.");
 *
 * DISPATCH.addMethod(READ, (self, process, args) -> ... args.string("path") ... args.string("charset") ...);
 * }</pre>
 *
 * Signatures are immutable and meant to be kept in static fields; building one needs no running environment.
 */
public final class Signature {

    private final String name;
    private final ParameterLayout layout;
    private final List<ScriptType> types;
    private final List<Object> defaults;
    private final @Nullable ScriptType returns;
    private final String help;
    private final String callForm;

    private Signature(final Builder builder, final String help) {
        this.name = builder.name;
        this.layout = ParameterLayout.of(builder.names, builder.kinds);
        this.types = List.copyOf(builder.types);
        this.defaults = Collections.unmodifiableList(new ArrayList<>(builder.defaults));
        this.returns = builder.returns;
        this.help = help;
        this.callForm = buildCallForm();
    }

    /**
     * Starts the signature of a method.
     *
     * @param name the name of the method, as scripts call it
     * @return a builder; add the parameters in their order, then finish with {@link Builder#help(String)}
     */
    public static @NotNull Builder method(final @NotNull String name) {
        return new Builder(name);
    }

    /**
     * @return the name of the method
     */
    public @NotNull String name() {
        return name;
    }

    /**
     * @return the parameters and the rules for matching arguments to them
     */
    public @NotNull ParameterLayout layout() {
        return layout;
    }

    /**
     * @return the call form, e.g. {@code read(path, charset = "UTF-8")}
     */
    public @NotNull String callForm() {
        return callForm;
    }

    /**
     * @return the HELP text: the call form, a colon, and the description
     */
    public @NotNull String helpText() {
        return callForm + ": " + help;
    }

    /**
     * @return the parameters for the {@code PARAMETERS} metadata; {@code *rest} and {@code **options} are named with their stars
     */
    public @NotNull List<ScriptType.Parameter> parameters() {
        final List<ScriptType.Parameter> parameters = new ArrayList<>(layout.size());
        for (int i = 0; i < layout.size(); i++) {
            parameters.add(ScriptType.parameter(written(i), types.get(i)));
        }
        return List.copyOf(parameters);
    }

    /**
     * @return what the method returns, or null if the signature does not say
     */
    public @Nullable ScriptType returns() {
        return returns;
    }

    /**
     * Binds the arguments of a call to the parameters.
     *
     * @param process   the running script
     * @param arguments the arguments of the call
     * @return the arguments, by parameter name
     * @throws JanitorArgumentException if arguments are missing, surplus, given twice, or have a name that matches nothing
     */
    public @NotNull SignatureArguments bind(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorArgumentException {
        return new SignatureArguments(this, process, arguments.getFunctionName(), layout.match(process, arguments));
    }

    /**
     * @param index the position of a parameter
     * @return its default value as a Java value (null, a String, Long, Double or Boolean); null for parameters without one
     */
    @Nullable Object defaultValue(final int index) {
        return defaults.get(index);
    }

    int indexOf(final String parameterName) {
        final int index = layout.indexOf(parameterName);
        if (index < 0) {
            throw new IllegalArgumentException(name + " has no parameter " + parameterName);
        }
        return index;
    }

    private String written(final int index) {
        return switch (layout.kind(index)) {
            case REQUIRED, OPTIONAL -> layout.name(index);
            case REST -> "*" + layout.name(index);
            case OPTIONS -> "**" + layout.name(index);
        };
    }

    private String buildCallForm() {
        final StringBuilder form = new StringBuilder(name).append('(');
        for (int i = 0; i < layout.size(); i++) {
            if (i > 0) {
                form.append(", ");
            }
            form.append(written(i));
            if (layout.kind(i) == ParameterKind.OPTIONAL) {
                form.append(" = ").append(literal(defaults.get(i)));
            }
        }
        return form.append(')').toString();
    }

    private static String literal(final @Nullable Object value) {
        if (value instanceof String text) {
            return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return callForm;
    }

    /**
     * Collects the parts of a signature. Parameters are added in their order: required ones, optional ones, then at most one
     * {@code *rest} and one {@code **options}.
     */
    public static final class Builder {
        private final String name;
        private final List<String> names = new ArrayList<>();
        private final List<ParameterKind> kinds = new ArrayList<>();
        private final List<ScriptType> types = new ArrayList<>();
        private final List<Object> defaults = new ArrayList<>();
        private @Nullable ScriptType returns;

        private Builder(final String name) {
            this.name = name;
        }

        private Builder add(final String parameterName, final ParameterKind kind, final ScriptType type, final @Nullable Object defaultValue) {
            names.add(parameterName);
            kinds.add(kind);
            types.add(type);
            defaults.add(defaultValue);
            return this;
        }

        /**
         * Adds a parameter that callers must give, by position or by name.
         *
         * @param parameterName the name
         * @param type          its type, for editors and the documentation
         * @return this builder
         */
        public @NotNull Builder param(final @NotNull String parameterName, final @NotNull ScriptType type) {
            return add(parameterName, ParameterKind.REQUIRED, type, null);
        }

        /**
         * Adds a parameter with a default value.
         *
         * @param parameterName the name
         * @param type          its type, for editors and the documentation
         * @param defaultValue  the value if the caller gives none: null, a String, a Long or Integer, a Double, or a Boolean
         * @return this builder
         */
        public @NotNull Builder optional(final @NotNull String parameterName, final @NotNull ScriptType type, final @Nullable Object defaultValue) {
            final Object value = defaultValue instanceof Integer number ? Long.valueOf(number) : defaultValue;
            if (value != null && !(value instanceof String || value instanceof Long || value instanceof Double || value instanceof Boolean)) {
                throw new IllegalArgumentException("unsupported default value for " + parameterName + ": " + value.getClass().getSimpleName());
            }
            return add(parameterName, ParameterKind.OPTIONAL, type, value);
        }

        /**
         * Adds {@code *name}, which collects the positional arguments that are left in a list.
         *
         * @param parameterName the name
         * @param elementType   the type of the elements
         * @return this builder
         */
        public @NotNull Builder rest(final @NotNull String parameterName, final @NotNull ScriptType elementType) {
            return add(parameterName, ParameterKind.REST, ScriptType.listOf(elementType), null);
        }

        /**
         * Adds {@code **name}, which collects the named arguments that match no parameter in a map.
         *
         * @param parameterName the name
         * @return this builder
         */
        public @NotNull Builder options(final @NotNull String parameterName) {
            return add(parameterName, ParameterKind.OPTIONS, ScriptType.MAP, null);
        }

        /**
         * @param type what the method returns
         * @return this builder
         */
        public @NotNull Builder returns(final @NotNull ScriptType type) {
            this.returns = type;
            return this;
        }

        /**
         * Finishes the signature.
         *
         * @param description what the method does, without the call form, which the signature writes itself
         * @return the signature
         * @throws IllegalArgumentException if a parameter name is used twice or the parameters are in an order that is not allowed
         */
        public @NotNull Signature help(final @NotNull String description) {
            return new Signature(this, description);
        }
    }
}
