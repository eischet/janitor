package com.eischet.janitor.api.scripting;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * A dispatcher for JanitorObjects.
 * @param <T> the type of JanitorObject
 */
@FunctionalInterface
public interface Dispatcher<T extends JanitorObject> {

    /**
     * Dispatches a method call to the appropriate method on the given JanitorObject.
     * @param instance the JanitorObject to dispatch the method call to
     * @param process the running script
     * @param name the name of the method to call
     * @return the result of the method call
     */
    JanitorObject dispatch(final T instance, final JanitorScriptProcess process, final String name);

    /**
     * Creates a combined dispatcher from a "parent" and a "child", or "superclass" and "subclass" if you prefer.
     * @param parent the parent dispatcher
     * @param child the child dispatcher
     * @return a new dispatcher that first tries to dispatch to the child, and then to the parent
     * @param <P> the type of the parent JanitorObject
     * @param <C> the type of the child JanitorObject
     */
    static <P extends JanitorObject, C extends JanitorObject> Dispatcher<P> inherit(Dispatcher<P> parent, Dispatcher<C> child) {
        return (instance, process, name) -> {
            @SuppressWarnings("unchecked") final JanitorObject result = child.dispatch((C) instance, process, name);
            if (result != null) {
                return result;
            } else {
                return parent.dispatch(instance, process, name);
            }
        };
    }

}
