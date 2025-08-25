package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.types.JanitorObject;

/**
 * Simpler specialization of ValueExpander for property types that are Janitor objects by themselves, e.g.
 * when you've got an object property whose type is a JanitorObject. In this case, expand value is simpler
 * to implement as returning the object itself, not having to do any casts in a best case scenario.
 * @param <T> some object type that is a JanitorObject
 */
public interface ScriptableValueExpander<T extends JanitorObject> extends ValueExpander<T, JanitorObject>  {
}
