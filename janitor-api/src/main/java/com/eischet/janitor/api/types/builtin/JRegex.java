// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;

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
    private JRegex(final WrapperDispatchTable<Pattern> dispatch, final Pattern pattern) {
        super(dispatch, pattern);
    }

    @Override
    public @NotNull String janitorToString() {
        return wrapped.toString();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "regex";
    }

    /**
     * Creates a new regular expression value.
     * @param dispatch the dispatch table for regular expressions
     * @param pattern the compiled pattern
     * @return the new value
     */
    public static JRegex newInstance(final WrapperDispatchTable<Pattern> dispatch, final Pattern pattern) {
        return new JRegex(dispatch, pattern);
    }
}
