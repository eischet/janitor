// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

/** The list of parameters in the declaration of a function. */
public class FormalParameters implements Iterable<FormalParameter> {

    private final List<FormalParameter> parameters;

    protected FormalParameters(final List<FormalParameter> parameters) throws CompilerError {
        this.parameters = parameters;

        // System.err.println("FormalParameters: " + parameters);

        final Set<String> seen = new HashSet<>();
        // check for plausibility
        FormalParameter.Kind previousKind = FormalParameter.Kind.POSITIONAL;
        for (final var parameter : parameters) {
            // System.out.println(parameter + " (" + parameter.getKind().title() + ")");
            // fail on any duplicate names
            if (seen.contains(parameter.getName())) {
                throw new CompilerError("duplicate parameter name: " + parameter.getName());
            }
            seen.add(parameter.getName());
            // fail when the order of paramters types is not correct
            final FormalParameter.Kind nextKind = parameter.getKind();
            if (!mayTransition(previousKind, nextKind)) {
                throw new CompilerError("invalid parameter order: " + parameter.getName() + " is " + nextKind.title() + " but this is not allowed after " + previousKind.title());
            }
            previousKind = nextKind;
        }
    }

    private boolean mayTransition(final FormalParameter.Kind currentKind, final FormalParameter.Kind nextKind) {
        if (currentKind == FormalParameter.Kind.POSITIONAL) {
            return true; // a positional parameter may be followed by any type of parameter
        } else if (currentKind == FormalParameter.Kind.DEFAULTED) {
            return nextKind != FormalParameter.Kind.POSITIONAL; // all but positional arguments may follow a defaulted argument
        } else if (currentKind == FormalParameter.Kind.VARARGS) {
            return nextKind == FormalParameter.Kind.KWARGS; // only kwargs may follow a varargs parameter
        } else if (currentKind == FormalParameter.Kind.KWARGS) {
            return false; // nothing may follow a kwargs parameter
        }
        throw new CompilerError("unknown parameter kind: " + currentKind);
    }

    /**
     * Creates a parameter list.
     * @param parameters the parameters
     * @return the parameter list
     * @throws CompilerError if the parameters are invalid, e.g. when names are duplicated
     */
    public static FormalParameters of(final List<FormalParameter> parameters) throws CompilerError {
        return new FormalParameters(List.copyOf(parameters));
    }

    /**
     * @return an empty parameter list
     */
    public static FormalParameters empty() {
        return new FormalParameters(Collections.emptyList());
    }

    /**
     * @return all parameters
     */
    public @NotNull @Unmodifiable List<FormalParameter> getParameters() {
        return parameters;
    }

    /**
     * @return the number of parameters
     */
    public int size() {
        return parameters.size();
    }

    /**
     * @return the parameters that callers must always supply
     */
    public @NotNull @Unmodifiable List<FormalParameter> getPositionalParameters() {
        return parameters.stream().filter(FormalParameter::isMinimallyRequired).toList();
    }

    @Override
    public String toString() {
        return parameters.stream().map(FormalParameter::toString).collect(Collectors.joining(", "));
    }

    /**
     * @return the number of parameters that callers must always supply
     */
    public int minSize() {
        return (int) parameters.stream().filter(it -> it.isMinimallyRequired()).count();
    }

    @Override
    public @NotNull Iterator<FormalParameter> iterator() {
        return parameters.iterator();
    }

    /**
     * @param index the index of the parameter
     * @return the parameter at the given index
     */
    public FormalParameter get(final int index) {
        return parameters.get(index);
    }

}
