package com.eischet.janitor.cleanup.api.api.filter;

import com.eischet.janitor.cleanup.api.api.types.JanitorObject;

import java.util.function.Predicate;

public interface FilterPredicate extends Predicate<JanitorObject> {
    FilterScript getFilterScript();
}
