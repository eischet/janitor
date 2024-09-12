package com.eischet.janitor.api.types.dispatch;

public interface JsonSupport<U> extends JsonSupportDelegateRead<U>, JsonSupportDelegateWrite<U>, JsonSupportDelegateDefault<U> {
}
