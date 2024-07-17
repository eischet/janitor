package com.eischet.janitor.toolbox.memory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Memory<T> {

    private final Set<Fact<T>> facts;
    private final long msecTerm;

    public Memory(final long term, final TimeUnit unit) {
        this.msecTerm = TimeUnit.MILLISECONDS.convert(term, unit);
        this.facts = ConcurrentHashMap.newKeySet();
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    public void remember(T value) {
        final long now = now();
        facts.removeIf(fact -> fact.getTimestamp() < now || Objects.equals(fact.getValue(), value));
        facts.add(new Fact<>(value, now + msecTerm));
    }

    public Stream<T> stream() {
        final long now = now();
        facts.removeIf(fact -> fact.getTimestamp() < now);
        return facts.stream().map(Fact::getValue);
    }

}
