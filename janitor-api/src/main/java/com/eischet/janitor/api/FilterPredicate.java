package com.eischet.janitor.api;

import com.eischet.janitor.api.types.JanitorObject;

import java.util.function.Predicate;

/**
 * A predicate that filters JanitorObjects, e.g. in a stream.
 * <p>
 * The interpreter provides tooling to quickly construct these from scripts, so you can apply user-written filters
 * to your own data, e.g. Stream#filter.
 * </p>
 * <p>
 * If this were TypeScript, the type signature would better be: Predicate&lt;JanitorObject|JanitorAware&gt;.
 * But it's not, so it isn't. When streaming JanitorAware objects, a simple .map(JanitorAware::getJanitorObject) will
 * suffice, though.
 * </p>
 * <p>com.eischet.janitor.env.FilterScript implements this interface. That code is not visible from here, so we
 * cannot link directly.</p>
 */
@FunctionalInterface
public interface FilterPredicate extends Predicate<JanitorObject> {
}
