package com.eischet.janitor.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

public class FormalParameters implements Iterable<FormalParameter> {

    private final List<FormalParameter> parameters;

    protected FormalParameters(final List<FormalParameter> parameters) throws RuntimeException {
        this.parameters = parameters;

        final Set<String> seen = new HashSet<>();
        // check for plausibility
        FormalParameter.Kind previousKind = FormalParameter.Kind.POSITIONAL;
        for (final var parameter : parameters) {
            System.out.println(parameter + " (" + parameter.getKind().title() + ")");
            // fail on any duplicate names
            if (seen.contains(parameter.getName())) {
                throw new RuntimeException("duplicate parameter name: " + parameter.getName());
            }
            seen.add(parameter.getName());
            // fail when the order of paramters types is not correct
            final FormalParameter.Kind nextKind = parameter.getKind();
            if (!mayTransition(previousKind, nextKind)) {
                throw new RuntimeException("invalid parameter order: " + parameter.getName() + " is " + nextKind.title() + " but this is not allowed after " + previousKind.title());
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
        throw new RuntimeException("unknown parameter kind: " + currentKind);
    }

    public static FormalParameters of(final List<FormalParameter> parameters) throws RuntimeException {
        return new FormalParameters(List.copyOf(parameters));
    }

    public static FormalParameters empty() {
        return new FormalParameters(Collections.emptyList());
    }

    public @NotNull @Unmodifiable List<FormalParameter> getParameters() {
        return parameters;
    }

    public int size() {
        return parameters.size();
    }

    public @NotNull @Unmodifiable List<FormalParameter> getPositionalParameters() {
        return parameters.stream().filter(FormalParameter::isMinimallyRequired).toList();
    }

    @Override
    public String toString() {
        return parameters.stream().map(FormalParameter::toString).collect(Collectors.joining(", "));
    }

    public int minSize() {
        return (int) parameters.stream().filter(it -> it.isMinimallyRequired()).count();
    }

    @Override
    public @NotNull Iterator<FormalParameter> iterator() {
        return parameters.iterator();
    }

    public FormalParameter get(final int index) {
        return parameters.get(index);
    }

}
