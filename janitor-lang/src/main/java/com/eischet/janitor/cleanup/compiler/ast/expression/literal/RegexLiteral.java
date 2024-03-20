package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JRegex;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

import java.util.regex.Pattern;

public class RegexLiteral extends Literal {

    private final Pattern pattern;

    public RegexLiteral(final Location location, final String text) {
        super(location);
        pattern = Pattern.compile(text);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return new JRegex(pattern);
    }
}
