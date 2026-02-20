package com.eischet.janitor.orm.filter;

public class MalformedExpression extends RuntimeException {
    public MalformedExpression(final String message) {
        super(message);
    }
    public MalformedExpression(final String message, final Throwable cause) {
        super(message, cause);
    }
}
