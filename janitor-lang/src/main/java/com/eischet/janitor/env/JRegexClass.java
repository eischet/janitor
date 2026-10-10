// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Operations for regular expression objects. */
public class JRegexClass {

    /**
     * Script method {@code regex.extract(text)}: finds the first match in the text, and returns the first capture group of it.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the capture group, or null if the pattern does not match
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject extract(final JanitorWrapper<Pattern> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JString str = arguments.getString(0);
        final Matcher matcher = self.janitorGetHostValue().matcher(str.janitorGetHostValue());
        if (matcher.find()) {
            return Janitor.string(matcher.group(1));
        } else {
            return JNull.NULL;
        }
    }

    /**
     * Script method {@code regex.matcher(text)}: creates a matcher for the text.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the matcher
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject matcher(final JanitorWrapper<Pattern> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JString str = arguments.getString(0);
        final Matcher matcher = self.janitorGetHostValue().matcher(str.janitorGetHostValue());
        return new JMatcherWrapper(matcher);
    }


    /**
     * Script method {@code regex.extractAll(text)}: finds all matches in the text, and returns the first capture group of each.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return a list of the capture groups
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject extractAll(final JanitorWrapper<Pattern> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JList list = Janitor.list();
        final JString str = arguments.getString(0);
        final Matcher matcher = self.janitorGetHostValue().matcher(str.janitorGetHostValue());
        while (matcher.find()) {
            list.add(Janitor.string(matcher.group(1)));
        }
        return list;
    }

    /**
     * Script method {@code regex.replaceFirst(text, replacement)}: replaces the first match in the text.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the new text
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject replaceFirst(final JanitorWrapper<Pattern> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final JString string = arguments.getString(0);
        final JString with = arguments.getString(1);
        final Matcher matcher = self.janitorGetHostValue().matcher(string.janitorGetHostValue());
        return Janitor.string(matcher.replaceFirst(with.janitorToString()));
    }

    /**
     * Script method {@code regex.replaceAll(text, replacement)}: replaces all matches in the text.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the new text
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject replaceAll(final JanitorWrapper<Pattern> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final JString string = arguments.getString(0);
        final JString with = arguments.getString(1);
        final Matcher matcher = self.janitorGetHostValue().matcher(string.janitorGetHostValue());
        return Janitor.string(matcher.replaceAll(with.janitorToString()));
    }

    /**
     * Script method {@code regex.split(text)}: splits the text at the matches of the pattern.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return a list of the parts
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject split(final JanitorWrapper<Pattern> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JList list = Janitor.list();
        final JString str = arguments.getString(0);
        final String[] parts = str.janitorGetHostValue().split(self.janitorGetHostValue().pattern());
        for (final String part : parts) {
            list.add(Janitor.string(part));
        }
        return list;
    }


    /**
     * Registers the standard methods and properties of regular expressions.
     * @param regexDispatcher the dispatch table to add them to
     */
    public static void applyDefaults(WrapperDispatchTable<Pattern> regexDispatcher) {
        regexDispatcher.addMethod("extract", JRegexClass::extract);
        regexDispatcher.addMethod("extractAll", JRegexClass::extractAll);
        regexDispatcher.addMethod("replaceAll", JRegexClass::replaceAll);
        regexDispatcher.addMethod("replaceFirst", JRegexClass::replaceFirst);
        regexDispatcher.addMethod("split", JRegexClass::split);
        regexDispatcher.addMethod("matcher", JRegexClass::matcher);
    }

}
