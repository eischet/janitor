package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * DateTime literal.
 */
public class DateTimeLiteral extends Literal {
    private final JDateTime constantDateTime;

    /**
     * Constructor.
     * @param location where
     * @param date what
     */
    public DateTimeLiteral(final Location location, final JDateTime date) {
        super(location);
        this.constantDateTime = date;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) {
        return constantDateTime;
    }

}
