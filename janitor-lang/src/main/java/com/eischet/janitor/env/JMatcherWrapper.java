// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;

import java.util.regex.Matcher;

/**
 * Wraps a Java regular expression {@link Matcher} for use in scripts, as returned by {@code regex.matcher(text)}.
 * <p>
 * {@code find()} moves to the next match, {@code matches()} tests the whole text; afterwards, {@code group}, {@code start}
 * and {@code end} describe the current match, or one of its groups, given by number or by name.
 * </p>
 */
public class JMatcherWrapper extends JanitorWrapper<Matcher> {

    private static final WrapperDispatchTable<Matcher> DISPATCH = new WrapperDispatchTable<>(null);

    /** Reads something about a group given by number. */
    @FunctionalInterface
    private interface ByIndex<T> {
        T get(Matcher matcher, int group);
    }

    /** Reads something about a group given by name. */
    @FunctionalInterface
    private interface ByName<T> {
        T get(Matcher matcher, String name);
    }

    static {
        // Every HELP text starts with the call form, "name(arguments): ", which the language reference relies on.
        DISPATCH.addMethod("find", (self, process, arguments) -> {
            arguments.require(0);
            return Janitor.toBool(self.janitorGetHostValue().find());
        }).setMetaData(Janitor.MetaData.HELP, "find(): Moves to the next match in the text and returns true, or returns false if there is none. Use it in a loop: while (m.find()) { ... }.");
        DISPATCH.addMethod("matches", (self, process, arguments) -> {
            arguments.require(0);
            return Janitor.toBool(self.janitorGetHostValue().matches());
        }).setMetaData(Janitor.MetaData.HELP, "matches(): Returns true if the whole text matches the pattern; then the whole text is the current match.");
        DISPATCH.addMethod("groupCount", (self, process, arguments) -> {
            arguments.require(0);
            return process.getBuiltins().integer(self.janitorGetHostValue().groupCount());
        }).setMetaData(Janitor.MetaData.HELP, "groupCount(): Returns the number of capture groups in the pattern.");
        DISPATCH.addMethod("group", (self, process, arguments) ->
                process.getBuiltins().nullableString(group(self, process, arguments, Matcher::group, Matcher::group))
        ).setMetaData(Janitor.MetaData.HELP, "group(group): Returns the text of the current match, or of a capture group given by number or name; null if the group did not take part in the match. group() and group(0) are the whole match.");
        DISPATCH.addMethod("start", (self, process, arguments) ->
                process.getBuiltins().integer(group(self, process, arguments, Matcher::start, Matcher::start))
        ).setMetaData(Janitor.MetaData.HELP, "start(group): Returns the index where the current match, or a capture group given by number or name, starts; -1 if the group did not take part in the match.");
        DISPATCH.addMethod("end", (self, process, arguments) ->
                process.getBuiltins().integer(group(self, process, arguments, Matcher::end, Matcher::end))
        ).setMetaData(Janitor.MetaData.HELP, "end(group): Returns the index after the end of the current match, or of a capture group given by number or name; -1 if the group did not take part in the match.");
    }

    /**
     * Reads something about the current match or one of its groups. The optional argument is the group's number or name;
     * without it, the whole match (group 0) is meant.
     */
    private static <T> T group(final JanitorWrapper<Matcher> self, final JanitorScriptProcess process, final JCallArgs arguments,
                               final ByIndex<T> byIndex, final ByName<T> byName) throws JanitorRuntimeException {
        arguments.require(0, 1);
        final Matcher matcher = self.janitorGetHostValue();
        try {
            if (arguments.size() == 0) {
                return byIndex.get(matcher, 0);
            }
            if (arguments.get(0) instanceof JString name) {
                return byName.get(matcher, name.janitorGetHostValue());
            }
            return byIndex.get(matcher, arguments.getRequiredIntValue(0));
        } catch (IllegalStateException e) {
            throw new JanitorNativeException(process, "there is no current match: call find() or matches() first, and check that it returned true", e);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw new JanitorArgumentException(process, "the pattern has no group " + arguments.get(0).janitorToString(), e);
        }
    }

    public JMatcherWrapper(final Matcher matcher) {
        super(DISPATCH, matcher);
    }
}
