// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The names and kinds of the parameters of a function, and the rules by which a call's arguments are matched to them. Script
 * functions and native methods with a {@link Signature} use the same rules, as in Python: positional arguments fill the
 * parameters in their order, then each named argument goes to the parameter of that name; {@code *rest} gets the positional
 * arguments that are left, {@code **options} the named arguments that match no parameter.
 */
public final class ParameterLayout {

    private final String[] names;
    private final ParameterKind[] kinds;
    private final int restIndex;
    private final int optionsIndex;

    private ParameterLayout(final String[] names, final ParameterKind[] kinds) {
        this.names = names;
        this.kinds = kinds;
        int rest = -1;
        int options = -1;
        for (int i = 0; i < kinds.length; i++) {
            if (kinds[i] == ParameterKind.REST) {
                rest = i;
            } else if (kinds[i] == ParameterKind.OPTIONS) {
                options = i;
            }
        }
        this.restIndex = rest;
        this.optionsIndex = options;
    }

    /**
     * Creates a layout.
     *
     * @param names the names of the parameters
     * @param kinds their kinds, in the same order
     * @return the layout
     * @throws IllegalArgumentException if a name is used twice, or the kinds are in an order that is not allowed (e.g. a
     *                                  required parameter after an optional one)
     */
    public static @NotNull ParameterLayout of(final @NotNull List<String> names, final @NotNull List<ParameterKind> kinds) {
        if (names.size() != kinds.size()) {
            throw new IllegalArgumentException("every parameter needs a name and a kind");
        }
        final Set<String> seen = new HashSet<>();
        ParameterKind previous = ParameterKind.REQUIRED;
        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            final ParameterKind kind = kinds.get(i);
            if (!seen.add(name)) {
                throw new IllegalArgumentException("duplicate parameter name: " + name);
            }
            if (!previous.mayBeFollowedBy(kind)) {
                throw new IllegalArgumentException("invalid parameter order: " + name + " is " + kind.title() + " but this is not allowed after " + previous.title());
            }
            previous = kind;
        }
        return new ParameterLayout(names.toArray(String[]::new), kinds.toArray(ParameterKind[]::new));
    }

    /**
     * @return the number of parameters
     */
    public int size() {
        return names.length;
    }

    /**
     * @param index the position of a parameter
     * @return its name
     */
    public @NotNull String name(final int index) {
        return names[index];
    }

    /**
     * @param index the position of a parameter
     * @return its kind
     */
    public @NotNull ParameterKind kind(final int index) {
        return kinds[index];
    }

    /**
     * @param name the name of a parameter
     * @return its position, or -1 if there is no parameter of that name
     */
    public int indexOf(final @NotNull String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Matches the arguments of a call to the parameters. All named arguments count as used
     * ({@link JCallArgs#requireNamesUsed()}), because a name that matches nothing fails here already.
     *
     * @param process   the running script
     * @param arguments the arguments of the call
     * @return the value of each parameter, in the order of the parameters; null where an optional parameter got no value. The
     * value of {@code *rest} is a list, that of {@code **options} a map, both possibly empty.
     * @throws JanitorArgumentException if arguments are missing, surplus, or given twice
     */
    public JanitorObject @NotNull [] match(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorArgumentException {
        final JanitorObject[] values = new JanitorObject[names.length];
        final int positionalCount = arguments.size();
        int position = 0;
        for (int i = 0; i < values.length; i++) {
            final ParameterKind kind = kinds[i];
            if (kind == ParameterKind.REST) {
                final JList rest = Janitor.list();
                while (position < positionalCount) {
                    rest.add(arguments.get(position++));
                }
                values[i] = rest;
            } else if (kind == ParameterKind.OPTIONS) {
                values[i] = Janitor.map();
            } else if (position < positionalCount) {
                values[i] = arguments.get(position++);
            }
        }
        if (restIndex < 0 && position < positionalCount) {
            throw new JanitorArgumentException(process, "%s: accepts at most %s positional arguments, but got %s".formatted(arguments.getFunctionName(), position, positionalCount));
        }
        if (arguments.hasNamed()) {
            for (final EvaluatedArgument named : arguments.takeNamed()) {
                final int index = indexOfNamed(named.getName());
                final JanitorObject value = named.getValue().janitorUnpack();
                if (index >= 0) {
                    if (values[index] != null) {
                        throw new JanitorArgumentException(process, "%s: more than one value for '%s'".formatted(arguments.getFunctionName(), named.getName()));
                    }
                    values[index] = value;
                } else if (optionsIndex >= 0) {
                    ((JMap) values[optionsIndex]).put(named.getName(), value);
                } else {
                    throw new JanitorArgumentException(process, "%s: unexpected named argument '%s'".formatted(arguments.getFunctionName(), named.getName()));
                }
            }
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null && kinds[i] == ParameterKind.REQUIRED) {
                throw new JanitorArgumentException(process, "%s: missing argument '%s'".formatted(arguments.getFunctionName(), names[i]));
            }
        }
        return values;
    }

    private int indexOfNamed(final String name) {
        for (int i = 0; i < names.length; i++) {
            if ((kinds[i] == ParameterKind.REQUIRED || kinds[i] == ParameterKind.OPTIONAL) && names[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
