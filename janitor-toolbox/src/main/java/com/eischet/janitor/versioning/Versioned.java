// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.versioning;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Something that can have different values for different versions.
 * @param <T> the type of the value
 */
public interface Versioned<T> {

    /**
     * @param version the version
     * @return the value that applies to the version, or null if there is none
     */
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

        /**
         * Calls the consumer for each version range and its value.
         * @param consumer receives the ranges and values
         */
        public void forEach(BiConsumer<VersionRange, X> consumer) {
            entries.forEach(entry -> consumer.accept(entry.getRange(), entry.getValue()));
        }

        /**
         * @return the lowest version at which any value starts to apply, or null if there is none
         */
        public @Nullable Version minimumVersion() {
            return entries.stream().map(entry -> entry.getRange().getMin()).filter(Objects::nonNull).min(Version::compareTo).orElse(null);
        }

        /**
         * @return the highest version at which any value stops to apply, or null if there is none
         */
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

            /**
             * @return the range of versions that the value applies to
             */
            public VersionRange getRange() {
                return range;
            }

            /**
             * @return the value
             */
            public X getValue() {
                return value;
            }
        }

        private final List<Entry> entries = new ArrayList<>(2);

        /**
         * Adds a value for a range of versions.
         * @param range the versions that the value applies to
         * @param value the value
         * @return this object
         */
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

    /**
     * @param value the value
     * @param <X> the type of the value
     * @return a value that applies to every version
     */
    static <X> Versioned<X> any(final X value) {
        return new Unversioned<>(value);
    }

}
