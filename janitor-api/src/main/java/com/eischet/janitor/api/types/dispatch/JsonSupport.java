package com.eischet.janitor.api.types.dispatch;

/**
 * Combines everything needed to handle a value in JSON: reading, writing and detecting default values.
 * @param <U> the type of the value
 */
public interface JsonSupport<U> extends JsonSupportDelegateRead<U>, JsonSupportDelegateWrite<U>, JsonSupportDelegateDefault<U> {
}
