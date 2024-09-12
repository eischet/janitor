package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Helper interface for looking up attributes from an optional parent dispatcher.
 *
 * @param <T> parent's type
 */
public interface DispatchDelegate<T> {
    JanitorObject delegate(final T instance, final JanitorScriptProcess process, final String name) throws JanitorRuntimeException;
}
