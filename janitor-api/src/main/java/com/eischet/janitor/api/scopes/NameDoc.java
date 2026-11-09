// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.scopes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The documentation of a name in a script, taken from the doc comment ({@code /** ... *}{@code /}) in front of the statement
 * that defines it, e.g. {@code /** Timeout in seconds *}{@code / timeout = 30;}.
 * <p>
 * Documentation belongs to names, not to values: the scope that holds a variable also holds its documentation
 * ({@link Scope#getNameDoc(String)}). A copy of a value does not carry it along, and the same value may be documented
 * differently under different names. Assigning a new value without a doc comment keeps the documentation.
 * </p>
 *
 * @param text       the text of the doc comment, or null if there is none
 * @param signature  the call form of a function, e.g. {@code "find(id, timeout = 30)"}, or null if the name is not
 *                   defined as a function
 * @param parameters the documented parameters of the function, in order
 * @param members    the documentation of keys of a map literal, or of members assigned like {@code foo.bar = 1}
 */
public record NameDoc(@Nullable String text, @Nullable String signature, @NotNull List<Param> parameters,
                      @NotNull Map<String, NameDoc> members) {

    /**
     * The documentation of a function parameter.
     *
     * @param name the name of the parameter, without {@code *} or {@code **}
     * @param text the text of its doc comment
     */
    public record Param(@NotNull String name, @NotNull String text) {
    }

    /**
     * Creates the documentation of a name.
     */
    public NameDoc {
        parameters = List.copyOf(parameters);
        members = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(members));
    }

    /**
     * @return true if there is nothing documented, not even a signature
     */
    public boolean isEmpty() {
        return text == null && signature == null && parameters.isEmpty() && members.isEmpty();
    }

    /**
     * @param name the name of a member
     * @return the documentation of the member, or null
     */
    public @Nullable NameDoc member(final @NotNull String name) {
        return members.get(name);
    }

    /**
     * Combines older documentation with newer documentation of the same name: what the newer one documents wins, what it
     * leaves out stays. The parameters go with the signature.
     *
     * @param newer the newer documentation
     * @return the combined documentation
     */
    public @NotNull NameDoc mergedWith(final @NotNull NameDoc newer) {
        final Map<String, NameDoc> mergedMembers = new LinkedHashMap<>(members);
        newer.members.forEach((key, doc) -> mergedMembers.merge(key, doc, NameDoc::mergedWith));
        final boolean newSignature = newer.signature != null;
        return new NameDoc(newer.text != null ? newer.text : text,
                newSignature ? newer.signature : signature,
                newSignature ? newer.parameters : parameters,
                mergedMembers);
    }

    /**
     * Wraps this documentation as a member below the given path, so that {@code foo.bar.baz} can be documented on {@code foo}.
     *
     * @param path the member names from the outside in, e.g. {@code ["bar", "baz"]}; empty returns this documentation
     * @return the wrapped documentation
     */
    public @NotNull NameDoc nestedIn(final @NotNull List<String> path) {
        NameDoc result = this;
        for (int i = path.size() - 1; i >= 0; i--) {
            result = new NameDoc(null, null, List.of(), Map.of(path.get(i), result));
        }
        return result;
    }

    /**
     * Renders the documentation as a HELP text: the signature, if any, followed by the text, then one line per documented
     * parameter, e.g. {@code "find(id, timeout = 30): Finds a customer.\n    id: the customer number"}.
     *
     * @return the HELP text, or null if neither a signature nor a text is documented
     */
    public @Nullable String render() {
        if (text == null && signature == null) {
            return null;
        }
        final StringBuilder out = new StringBuilder();
        if (signature != null) {
            out.append(signature);
            if (text != null) {
                out.append(": ").append(text);
            }
        } else {
            out.append(text);
        }
        for (final Param parameter : parameters) {
            out.append("\n    ").append(parameter.name()).append(": ").append(parameter.text().replace("\n", "\n        "));
        }
        return out.toString();
    }

    /**
     * Turns the raw text of a doc comment into its documentation text: removes {@code /**} (or {@code /*!} for the
     * documentation of a module) and the closing marker, the
     * leading {@code *} of each line and surrounding blank lines.
     *
     * @param comment the doc comment as written in the source code
     * @return the text, or null if the comment is empty
     */
    public static @Nullable String textOf(final @NotNull String comment) {
        String body = comment;
        if (body.startsWith("/**") || body.startsWith("/*!")) {
            body = body.substring(3);
        }
        if (body.endsWith("*/")) {
            body = body.substring(0, body.length() - 2);
        }
        final List<String> lines = new ArrayList<>();
        for (final String line : body.split("\\R", -1)) {
            String stripped = line.strip();
            if (stripped.startsWith("*")) {
                stripped = stripped.substring(1).strip();
            }
            lines.add(stripped);
        }
        while (!lines.isEmpty() && lines.getFirst().isEmpty()) {
            lines.removeFirst();
        }
        while (!lines.isEmpty() && lines.getLast().isEmpty()) {
            lines.removeLast();
        }
        return lines.isEmpty() ? null : String.join("\n", lines);
    }

}
