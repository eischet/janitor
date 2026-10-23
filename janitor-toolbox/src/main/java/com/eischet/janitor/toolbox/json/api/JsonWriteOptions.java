// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Immutable options that control how a {@link JsonOutputStream} writes JSON.
 * @param indent one indentation step, e.g. two spaces; the empty string produces compact output
 * @param nonFinite how NaN and infinite numbers are written
 * @param empty which values the {@code pair(...)} and {@code optional(...)} helpers leave out
 * @param omitConfidential true to leave out fields marked as confidential, e.g. passwords, when writing objects; this is up to the
 *                         writers, the stream itself does not know which fields are confidential
 */
public record JsonWriteOptions(@NotNull String indent, @NotNull NonFinitePolicy nonFinite, @NotNull EmptyPolicy empty, boolean omitConfidential) {

    /** Compact output, failing on NaN and infinite numbers, writing all values, including confidential ones. */
    public static final JsonWriteOptions COMPACT = new JsonWriteOptions("", NonFinitePolicy.FAIL, EmptyPolicy.WRITE, false);

    /** Like {@link #COMPACT}, but indented by two spaces per level. */
    public static final JsonWriteOptions PRETTY = COMPACT.withIndent("  ");

    /** How NaN and infinite numbers are written. */
    public enum NonFinitePolicy {
        /** Throw a {@link JsonException}, because JSON cannot represent these numbers. */
        FAIL,
        /** Write null instead. */
        NULL,
        /** Write the number as a string, e.g. "NaN" or "Infinity". */
        STRING
    }

    /** Which values the {@code pair(...)} and {@code optional(...)} helpers leave out, together with their key. */
    public enum EmptyPolicy {
        /** pair(...) writes null values; optional(...) leaves out nulls, empty lists and default or empty objects. */
        WRITE,
        /** Like WRITE, but pair(...) leaves out null values, too. */
        OMIT_NULL,
        /** Like OMIT_NULL, but empty strings are left out, too. */
        OMIT_EMPTY
    }

    /**
     * Creates options.
     * @param indent one indentation step; the empty string produces compact output
     * @param nonFinite how NaN and infinite numbers are written
     * @param empty which values the helpers leave out
     * @param omitConfidential true to leave out confidential fields
     */
    public JsonWriteOptions {
        Objects.requireNonNull(indent, "indent");
        Objects.requireNonNull(nonFinite, "nonFinite");
        Objects.requireNonNull(empty, "empty");
    }

    /**
     * Creates options that write confidential fields.
     * @param indent one indentation step; the empty string produces compact output
     * @param nonFinite how NaN and infinite numbers are written
     * @param empty which values the helpers leave out
     */
    public JsonWriteOptions(final @NotNull String indent, final @NotNull NonFinitePolicy nonFinite, final @NotNull EmptyPolicy empty) {
        this(indent, nonFinite, empty, false);
    }

    /**
     * @return true if the output is indented
     */
    public boolean isPretty() {
        return !indent.isEmpty();
    }

    /**
     * @param indent one indentation step; the empty string produces compact output
     * @return a copy of these options with the given indentation
     */
    public JsonWriteOptions withIndent(final @NotNull String indent) {
        return new JsonWriteOptions(indent, nonFinite, empty, omitConfidential);
    }

    /**
     * @param nonFinite how NaN and infinite numbers are written
     * @return a copy of these options with the given policy
     */
    public JsonWriteOptions withNonFinite(final @NotNull NonFinitePolicy nonFinite) {
        return new JsonWriteOptions(indent, nonFinite, empty, omitConfidential);
    }

    /**
     * @param empty which values the helpers leave out
     * @return a copy of these options with the given policy
     */
    public JsonWriteOptions withEmpty(final @NotNull EmptyPolicy empty) {
        return new JsonWriteOptions(indent, nonFinite, empty, omitConfidential);
    }

    /**
     * @param omitConfidential true to leave out confidential fields, false to write them
     * @return a copy of these options with the given setting
     */
    public JsonWriteOptions withOmitConfidential(final boolean omitConfidential) {
        return new JsonWriteOptions(indent, nonFinite, empty, omitConfidential);
    }

    /**
     * Checks whether the helpers leave out a string value under these options.
     * @param value the value, may be null
     * @return true if the value is left out
     */
    public boolean omits(final @Nullable String value) {
        return switch (empty) {
            case WRITE -> false;
            case OMIT_NULL -> value == null;
            case OMIT_EMPTY -> value == null || value.isEmpty();
        };
    }

}
