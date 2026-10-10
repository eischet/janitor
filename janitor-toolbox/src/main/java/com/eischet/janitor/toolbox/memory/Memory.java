// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.memory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Remembers values for a limited time. Values that are older than the term are forgotten.
 * @param <T> the type of the values
 */
public class Memory<T> {

    private final Set<Fact<T>> facts;
    private final long msecTerm;

    public Memory(final long term, final TimeUnit unit) {
        this.msecTerm = TimeUnit.MILLISECONDS.convert(term, unit);
        this.facts = ConcurrentHashMap.newKeySet();
    }

    /**
     * @return the current time in milliseconds, can be overridden for testing
     */
    protected long now() {
        return System.currentTimeMillis();
    }

    /**
     * Remembers a value, starting its term anew if it is already known.
     * @param value the value to remember
     */
    public void remember(T value) {
        final long now = now();
        facts.removeIf(fact -> fact.getTimestamp() < now || Objects.equals(fact.getValue(), value));
        facts.add(new Fact<>(value, now + msecTerm));
    }

    /**
     * Checks whether a value is currently remembered.
     * @param value the value
     * @return true if the value is remembered
     */
    public boolean contains(T value) {
        return stream().anyMatch(it -> Objects.equals(it, value));
    }

    /**
     * @return the number of values that are currently remembered
     */
    public int size() {
        stream();
        return facts.size();
    }

    /**
     * @return the values that are currently remembered
     */
    public Stream<T> stream() {
        final long now = now();
        facts.removeIf(fact -> fact.getTimestamp() < now);
        return facts.stream().map(Fact::getValue);
    }

}
