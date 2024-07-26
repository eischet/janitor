package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Today literal: @today.
 */
public class TodayLiteral extends Literal {

    /**
     * Constructor.
     * @param location where
     */
    public TodayLiteral(final Location location) {
        super(location);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return JDate.today();
    }
}
