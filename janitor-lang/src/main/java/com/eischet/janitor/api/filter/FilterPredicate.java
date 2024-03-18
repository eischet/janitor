package com.eischet.janitor.api.filter;

import com.eischet.janitor.api.types.JanitorObject;

import java.util.function.Predicate;

public interface FilterPredicate extends Predicate<JanitorObject> {
    FilterScript getFilterScript();
}
