package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Null literal: null.
 */
public class NullLiteral extends Literal {

    /**
     * Constructor.
     * @param location where
     */
    private NullLiteral(final Location location) {
        super(location);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        return JNull.NULL;
    }

    public static NullLiteral NULL = new NullLiteral(null);

}
