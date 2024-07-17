package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.traits.JConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A regex object, representing a regular expression.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JRegex implements JConstant {

    private final Pattern pattern;

    /**
     * Create a new JRegex.
     * @param pattern the pattern
     */
    public JRegex(final Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public Pattern janitorGetHostValue() {
        return pattern;
    }

    @Override
    public String janitorToString() {
        return pattern.toString();
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        // TODO: convert this into a proper dispatch table
        if ("extract".equals(name)) {
            return new JBoundMethod<>("extract", this, new JUnboundMethod<>() {
                @Override
                public JanitorObject call(final JRegex self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
                    arguments.require(1);
                    final JString str = arguments.getString(0);
                    final Matcher matcher = self.janitorGetHostValue().matcher(str.janitorGetHostValue());
                    if (matcher.find()) {
                        return JString.of(matcher.group(1));
                    } else {
                        return JNull.NULL;
                    }
                }
            });
        }
        if ("replaceAll".equals(name)) {
            return new JBoundMethod<>("replaceAll", this, new JUnboundMethod<>() {
                @Override
                public JanitorObject call(final JRegex self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
                    arguments.require(2);
                    final JString string = arguments.getString(0);
                    final JString with = arguments.getString(1);
                    final Matcher matcher = self.janitorGetHostValue().matcher(string.janitorGetHostValue());
                    return JString.of(matcher.replaceAll(with.janitorToString()));
                }
            });
        }
        if ("replaceFirst".equals(name)) {
            return new JBoundMethod<>("replaceFirst", this, new JUnboundMethod<>() {
                @Override
                public JanitorObject call(final JRegex self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
                    arguments.require(2);
                    final JString string = arguments.getString(0);
                    final JString with = arguments.getString(1);
                    final Matcher matcher = self.janitorGetHostValue().matcher(string.janitorGetHostValue());
                    return JString.of(matcher.replaceFirst(with.janitorToString()));
                }
            });
        }
        if ("extractAll".equals(name)) {
            return new JBoundMethod<>("extractAll", this, new JUnboundMethod<>() {
                @Override
                public JanitorObject call(final JRegex self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
                    arguments.require(1);
                    final JList list = new JList();
                    final JString str = arguments.getString(0);
                    final Matcher matcher = self.janitorGetHostValue().matcher(str.janitorGetHostValue());
                    while (matcher.find()) {
                        list.add(JString.of(matcher.group(1)));
                    }
                    return list;
                }
            });
        }
        if ("split".equals(name)) {
            return new JBoundMethod<>("split", this, new JUnboundMethod<>() {
                @Override
                public JanitorObject call(final JRegex self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
                    arguments.require(1);
                    final JList list = new JList();
                    final JString str = arguments.getString(0);
                    final String[] parts = str.janitorGetHostValue().split(self.janitorGetHostValue().pattern());
                    for (final String part : parts) {
                        list.add(JString.of(part));
                    }
                    return list;
                }
            });
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "regex";
    }

}
