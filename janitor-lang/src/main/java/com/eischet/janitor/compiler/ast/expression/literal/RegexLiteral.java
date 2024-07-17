package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JRegex;
import com.eischet.janitor.api.types.JanitorObject;

import java.util.regex.Pattern;

/**
 * Regex literal: re/foo.+/.
 */
public class RegexLiteral extends Literal {

    private final Pattern pattern;

    /**
     * Constructor.
     * @param location where
     * @param text what
     */
    public RegexLiteral(final Location location, final String text) {
        super(location);
        pattern = Pattern.compile(text);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return new JRegex(pattern);
    }
}
