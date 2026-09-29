package com.eischet.janitor.versioning;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public interface Versioned<T> {

    T getForVersion(Version version);

    class Unversioned<X> implements Versioned<X> {

        private final X value;

        public Unversioned(final X value) {
            this.value = value;
        }

        @Override
        public X getForVersion(final Version version) {
            return value;
        }

    }

    class Varying<X> implements Versioned<X> {

        public void forEach(BiConsumer<VersionRange, X> consumer) {
            entries.forEach(entry -> consumer.accept(entry.getRange(), entry.getValue()));
        }

        public @Nullable Version minimumVersion() {
            return entries.stream().map(entry -> entry.getRange().getMin()).filter(Objects::nonNull).min(Version::compareTo).orElse(null);
        }

        public @Nullable Version maximumVersion() {
            return entries.stream().map(entry -> entry.getRange().getMax()).filter(Objects::nonNull).max(Version::compareTo).orElse(null);
        }

        class Entry {
            final VersionRange range;
            final X value;

            public Entry(final VersionRange range, final X value) {
                this.range = range;
                this.value = value;
            }

            public VersionRange getRange() {
                return range;
            }

            public X getValue() {
                return value;
            }
        }

        private final List<Entry> entries = new ArrayList<>(2);

        public Varying<X> with(final VersionRange range, final X value) {
            entries.add(new Entry(range, value));
            return this;
        }

        @Override
        public X getForVersion(final Version version) {
            return entries.stream()
                .filter(entry -> entry.getRange().includes(version))
                .findFirst()
                .map(Entry::getValue)
                .orElse(null);
        }

    }

    static <X> Versioned<X> any(final X value) {
        return new Unversioned<>(value);
    }

}
