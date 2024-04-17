package com.eischet.janitor.api;

import com.eischet.janitor.api.types.JanitorObject;

import java.util.function.Predicate;

@FunctionalInterface
public interface FilterPredicate extends Predicate<JanitorObject> {
}
