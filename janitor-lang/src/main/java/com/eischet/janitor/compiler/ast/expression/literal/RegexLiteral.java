package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Regex literal: re/foo.+/.
 */
public class RegexLiteral extends Literal {

    private final Pattern pattern;
    private final String text;

    /**
     * Constructor.
     * @param location where
     * @param text what
     */
    public RegexLiteral(final Location location, final String text) {
        super(location);
        pattern = Pattern.compile(text);
        this.text = text;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        return process.getEnvironment().getBuiltinTypes().regex(pattern);
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject().optional("type", simpleClassNameOf(this)).optional("text", text).endObject();

    }
}
