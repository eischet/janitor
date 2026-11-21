// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

/**
 * The kinds of parameters a function can declare, in the order in which they may follow each other.
 */
public enum ParameterKind {
    /** A parameter that the caller must give, by position or by name, e.g. {@code path}. */
    REQUIRED("required"),
    /** A parameter with a default value, by position or by name, e.g. {@code charset = "UTF-8"}. */
    OPTIONAL("optional"),
    /** Collects the positional arguments that are left in a list, e.g. {@code *rest}. */
    REST("rest"),
    /** Collects the named arguments that match no parameter in a map, e.g. {@code **options}. */
    OPTIONS("options");

    private final String title;

    ParameterKind(final String title) {
        this.title = title;
    }

    /**
     * @return the name of the kind, for messages
     */
    public String title() {
        return title;
    }

    /**
     * @param next the kind of the parameter that follows
     * @return true if a parameter of that kind may follow one of this kind
     */
    public boolean mayBeFollowedBy(final ParameterKind next) {
        return switch (this) {
            case REQUIRED -> true;
            case OPTIONAL -> next != REQUIRED;
            case REST -> next == OPTIONS;
            case OPTIONS -> false;
        };
    }
}
