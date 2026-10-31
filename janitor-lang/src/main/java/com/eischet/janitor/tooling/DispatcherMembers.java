// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.tooling;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Lists the members of a dispatcher with what their HELP texts say, for documentation and editors.
 */
public final class DispatcherMembers {

    private DispatcherMembers() {
    }

    /**
     * A method or property of a dispatcher.
     *
     * @param name        the name
     * @param method      true for a method, false for a property
     * @param signature   the call form from the HELP text, e.g. "indexOf(value)", else "name(...)" or "name"
     * @param description the HELP text without the call form, empty if there is none
     * @param help        the HELP text as it is, null if there is none
     * @param wellFormed  true if the HELP text starts with the call form ("name(arguments): " or "name: ")
     */
    public record Member(@NotNull String name, boolean method, @NotNull String signature, @NotNull String description,
                         @Nullable String help, boolean wellFormed) {
    }

    /**
     * List the members of a dispatcher, sorted by name, without operator implementations like {@code __get__}.
     * @param dispatcher the dispatcher, may be null
     * @param excluded   names to leave out, e.g. those every object has
     * @return the members
     */
    public static @NotNull List<Member> of(final @Nullable Dispatcher<?> dispatcher, final @NotNull Set<String> excluded) {
        if (dispatcher == null) {
            return List.of();
        }
        final TreeSet<String> names = dispatcher.streamAttributeNames()
                .filter(name -> !name.startsWith("__"))
                .filter(name -> !excluded.contains(name))
                .collect(Collectors.toCollection(TreeSet::new));
        final List<Member> members = new ArrayList<>();
        for (final String name : names) {
            final boolean method = dispatcher.getMetaData(name, Janitor.MetaData.TYPE_HINT) == Janitor.MetaData.TypeHint.METHOD;
            members.add(member(name, method, dispatcher.getMetaData(name, Janitor.MetaData.HELP)));
        }
        return members;
    }

    /**
     * Split a HELP text into call form and description.
     * @param name   the member's name
     * @param method true for a method
     * @param help   the HELP text, may be null
     * @return the member
     */
    public static @NotNull Member member(final @NotNull String name, final boolean method, final @Nullable String help) {
        final String fallback = method ? name + "(...)" : name;
        if (help == null || help.isBlank()) {
            return new Member(name, method, fallback, "", help, false);
        }
        final String text = help.strip().replaceAll("\\s+", " ");
        if (method && text.startsWith(name + "(")) {
            final int end = text.indexOf("): ");
            if (end > 0) {
                return new Member(name, true, text.substring(0, end + 1), text.substring(end + 3), help, true);
            }
        }
        if (!method && text.startsWith(name + ": ")) {
            return new Member(name, false, name, text.substring(name.length() + 2), help, true);
        }
        return new Member(name, method, fallback, text, help, false);
    }
}
