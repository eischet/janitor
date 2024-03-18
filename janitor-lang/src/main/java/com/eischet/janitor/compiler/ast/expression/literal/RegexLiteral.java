package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JRegex;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;

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
