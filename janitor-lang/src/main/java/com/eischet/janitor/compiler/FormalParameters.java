package com.eischet.janitor.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class FormalParameters implements Iterable<FormalParameter> {

    private final List<FormalParameter> parameters;

    protected FormalParameters(final List<FormalParameter> parameters) {
        this.parameters = parameters;
    }

    public static FormalParameters of(final List<FormalParameter> parameters) {
        return new FormalParameters(List.copyOf(parameters));
    }

    public static FormalParameters empty() {
        return new FormalParameters(Collections.emptyList());
    }

    public @NotNull @Unmodifiable List<FormalParameter> getParameters() {
        return parameters;
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
