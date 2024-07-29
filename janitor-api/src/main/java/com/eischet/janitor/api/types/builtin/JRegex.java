package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.JanitorWrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A regex object, representing a regular expression.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JRegex extends JanitorWrapper<Pattern> implements JConstant {

    /**
     * Create a new JRegex.
     * @param pattern the pattern
     */
    private JRegex(final JanitorWrapperDispatchTable<Pattern> dispatch, final Pattern pattern) {
        super(dispatch, pattern);
    }

    @Override
    public String janitorToString() {
        return wrapped.toString();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "regex";
    }

    public static JRegex newInstance(final JanitorWrapperDispatchTable<Pattern> dispatch, final Pattern pattern) {
        return new JRegex(dispatch, pattern);
    }
}
