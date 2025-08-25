package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

public interface ValueExpander<T extends JanitorObject, U extends JanitorObject> {
    /**
     * Convert 'value' into an appropriate object type for assigning to a property.
     * The type of the property is not mentioned in this interface, but implied by the addObjectProperty call using this.
     * @param instance an object instance; this can be used e.g. to look up a DAO for resolving an integer ID into a real object, if you set it up like this
     * @param value any value that a script author might send our way, i.e., expect the unexpected
     * @return the proper value, as JanitorObject
     * @throws IllegalArgumentException when the value cannot be converted
     */
    @NotNull U expandValue(final @NotNull T instance, final @NotNull JanitorObject value) throws IllegalArgumentException;
}
