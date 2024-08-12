package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Date literal: @2024-07-21.
 */
public class DateLiteral extends Literal {
    private final JDate constantDate;

    /**
     * Constructor.
     * @param location where
     * @param date what
     */
    public DateLiteral(final Location location, final JDate date) {
        super(location);
        this.constantDate = date;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) {
        return constantDate;
    }

}
