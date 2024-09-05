package com.eischet.janitor.api.types.composed;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;

/**
 * Interface for objects that can be converted to a JanitorObject.
 * <p>
 * This is the simplest and least intrusive way of making a Java class compatible with the Janitor API.
 * </p>
 * <p>
 * When binding an object into a script's scope, the asJanitorObject method will automatically be called
 * to retrieve a wrapper object. You can implement this wrapper object separately. A good starting
 * point for this should be the JanitorWrapper class.
 * </p>
 * <p>
 * Note that this interface is <b>not required</b> to be used. It's merely a helper for having the
 * language automatically retrieve wrapper objects. You can always do that manually if you prefer,
 * and in cases where you cannot or do not want to modify the original class, there's little choice anyway.
 * </p>
 * <p>
 * Caching the object returned should not usually be necessary: if you're using the JanitorWrapper approach with
 * a (static) dispatch table, the returned object will usually only consist of two fields, that's a reference to
 * the original object and a reference to the dispatch table. Caching these lightweights is probably not necessary.
 * </p>
 *
 * @see com.eischet.janitor.api.types.wrapped.JanitorWrapper
 * @see WrapperDispatchTable
 */
public interface JanitorAware {

    /**
     * Convert this object to a JanitorObject.
     *
     * @return the JanitorObject
     */
    JanitorObject asJanitorObject();

}
