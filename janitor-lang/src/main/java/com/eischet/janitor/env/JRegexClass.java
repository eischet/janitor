package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JRegex;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JRegexClass {

    public static JanitorObject extract(final JanitorWrapper<Pattern> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JString str = arguments.getString(0);
        final Matcher matcher = self.janitorGetHostValue().matcher(str.janitorGetHostValue());
        if (matcher.find()) {
            return runningScript.getEnvironment().getBuiltins().string(matcher.group(1));
        } else {
            return JNull.NULL;
        }
    }

    public static JanitorObject extractAll(final JanitorWrapper<Pattern> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JList list = runningScript.getEnvironment().getBuiltins().list();
        final JString str = arguments.getString(0);
        final Matcher matcher = self.janitorGetHostValue().matcher(str.janitorGetHostValue());
        while (matcher.find()) {
            list.add(runningScript.getEnvironment().getBuiltins().string(matcher.group(1)));
        }
        return list;
    }

    public static JanitorObject replaceFirst(final JanitorWrapper<Pattern> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final JString string = arguments.getString(0);
        final JString with = arguments.getString(1);
        final Matcher matcher = self.janitorGetHostValue().matcher(string.janitorGetHostValue());
        return runningScript.getEnvironment().getBuiltins().string(matcher.replaceFirst(with.janitorToString()));
    }

    public static JanitorObject replaceAll(final JanitorWrapper<Pattern> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final JString string = arguments.getString(0);
        final JString with = arguments.getString(1);
        final Matcher matcher = self.janitorGetHostValue().matcher(string.janitorGetHostValue());
        return runningScript.getEnvironment().getBuiltins().string(matcher.replaceAll(with.janitorToString()));
    }

    public static JanitorObject split(final JanitorWrapper<Pattern> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JList list = runningScript.getEnvironment().getBuiltins().list();
        final JString str = arguments.getString(0);
        final String[] parts = str.janitorGetHostValue().split(self.janitorGetHostValue().pattern());
        for (final String part : parts) {
            list.add(runningScript.getEnvironment().getBuiltins().string(part));
        }
        return list;
    }


}
