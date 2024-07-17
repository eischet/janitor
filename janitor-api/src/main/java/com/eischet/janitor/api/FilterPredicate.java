package com.eischet.janitor.api;

import com.eischet.janitor.api.types.JanitorObject;

import java.util.function.Predicate;

/**
 * A predicate that filters JanitorObjects.
 * The interpreter provides tooling to quickly construct these from scripts, so you can apply user-written filters
 * to your own data, e.g. Stream#filter.
 */
@FunctionalInterface
public interface FilterPredicate extends Predicate<JanitorObject> {
}
