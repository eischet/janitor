package com.eischet.janitor.api.types;


import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A JanitorObject is a value that can be used in a Janitor script.
 * <p>
 * All objects used by scripts must implement this interface.
 * </p>
 * <p>
 * The interface is designed to be as simple as possible, and to be easy to implement.
 * Notably, we does not deal with classes at this level.
 * </p>
 * <p>
 * I've seen scripting languages that work with Java's Object directly, e.g. <a href="https://github.com/mozilla/rhino/blob/master/src/org/mozilla/javascript/Slot.java">Mozilla Rhino</a>.
 * That's a valid approach for sure, but when interfacing with Rhino scripts I personally found it a bit cumbersome to start at "Object" every time I retrieve something from a script.
 * </p>
 * <p>
 * All methods have been prefixed with "janitor", not because I enjoy reading the name all over the place, but because this helps avoid
 * naming collisions with existing code when you're adapting or wrapping classes to work with the scripting language.
 * </p>
 */
public interface JanitorObject {

    /**
     * Return the value of this object in the host language.
     * <p>Intended Users: host (Java) code.</p>
     * <p>By default, this is the object itself.</p>
     * <p>
     * However, in order to be more useful, some objects return their internal value instead to allow access to that. Notably, JString and JInt do this.
     * Note that is it a good idea to change "Object" to the real type to help the Java-side user avoid additional casts and instanceof checks.
     * </p>
     * @return the Java value of this object
     */
    default Object janitorGetHostValue() {
        return this;
    }

    /**
     * Return true if this object is considered true in a boolean context.
     * <p>Intended Users: host (Java) code, scripting runtime.</p>
     * <p>
     * This is the default. If your derived class can be "falsy", override this method accordingly.
     * JList does that, for example, in order to make empty lists "falsy"
     * </p>
     * @return the "truthiness" of this object
     */
    default boolean janitorIsTrue() {
        return true;
    }

    /**
     * Return the value of this object as a string.
     * <p>Intended Users: host (Java) code.</p>
     * <p>
     * This idea method is similar to Python's __repr__ method: provide a string representation of the object that can be used to reconstruct it, if possible.
     * See JDate and JDateTime for examples of this.
     * </p>
     * <p>Objects are not required to provide a meaningful string representation, and it's expected that not all types of objects can be constructed <i>within</i> a script.</p>
     * <p>This representation is supposed to be used within the host (Java) code, not within scripts themselves.</p>
     * <p>By default, this is delegated to the Object::toString() method.</p>
     * @return a string representation of the object, preferably one that can be used inside a script to reconstruct the object, if applicable
     */
    default String janitorToString() {
        return toString();
    }

    /**
     * Return the name of the type of this object.
     * <p>Intended Users: host (Java) code, scripting runtime.</p>
     * <p>
     * This value is accessible in scripts as the class attribute.
     * For example: (17).type == 'int'. Also, maybe surprisingly, null.type == "null".
     * </p>
     * <p>
     * It it not a requirement that this type name is actually usable within a script.
     * By default, the simple class name of this object is returned.
     * </p>
     * @return the type name of an object
     */
    default @NotNull String janitorClassName() {
        return getClass().getSimpleName();
    }

    /**
     * If this object is a container/wrapper for another object, return that object.
     * <p>Intended Users: scripting runtime.</p>
     * <p>
     *     This helps with implementing wrapper classes, e.g. for object properties that are themselves class instances containing single values.
     *     Most people will probably not bother with this, but I'm using it for some data holder classes.
     * </p>
     * <p>By default, we assume that this object itself is useful for scripts, and return "this".</p>
     * @return usually this, or a preferred value contained by this object
     */
    default JanitorObject janitorUnpack() {
        return this;
    }

    /**
     * Called on an Object when it leaves a scripting scope.
     * <p>Intended Users: scripting runtime.</p>
     * <p>
     * This is a chance to release resources for objects that were <b>created by the script</b>.
     * Global objects passed into a script should not react to this event to avoid becoming unusable in case they are reused!
     * </p>
     */
    default void janitorLeaveScope() {
        // ignored by default
    }

    /**
     * Retrieve an attribute of an object, e.h. a method or a property.
     * <p>Intended Users: scripting runtime.</p>
     * @param runningScript a running script processed
     * @param name the name of the attribute
     * @param required whether the attribute is required to exist
     * @return the attribute, or null if it does not exist and is not required
     * @throws JanitorNameException if the attribute does not exist but is required
     */
    @Nullable
    default JanitorObject janitorGetAttribute(JanitorScriptProcess runningScript, String name, final boolean required) throws JanitorNameException {
        // First, try delegating the lookup to a wrapped object:
        final @Nullable JanitorObject innerConstant = janitorUnpack();
        if (innerConstant != this && innerConstant != null) {
            return innerConstant.janitorGetAttribute(runningScript, name, required);
        }
        // Handle the class attribute here, previously called _type (which should be removed from older scripts which use it).
        // LATER get rid of "_type".
        if ("_type".equals(name) || "class".equals(name)) {
            return JString.of(janitorClassName());
        }
        if (required) {
            final Object hv = janitorGetHostValue();
            final Class<?> ht = hv == null ? null : hv.getClass();

            String stringRep = null;
            if (this instanceof JString) {
                stringRep = "String";
            } else {
                stringRep = toString();
            }

            throw new JanitorNameException(runningScript, "invalid method '%s' on %s (%s->%s)".formatted(name, stringRep, this.getClass(), ht));
        } else {
            return null;
        }
    }

    /**
     * Try to convert this object into the given type.
     * <p>Intended Users: scripting runtime, host (Java) code that implements functions/classes for scripts.</p>
     * @param type the type needed by the caller
     * @return an instance of that type, derived from this object or an inner object, when compatible, or null when not compatible
     * @param <T> the type required by the caller
     */
    default <T extends JanitorObject> @Nullable T janitorCoerce(final Class<T> type) {
        if (type.isAssignableFrom(this.getClass())) {
            return type.cast(this);
        } else {
            // LATER: we might need to fully unpack this in a loop!?
            final JanitorObject unpacked = janitorUnpack();
            if (unpacked != this && unpacked != null) {
                if (type.isAssignableFrom(unpacked.getClass())) {
                    return type.cast(unpacked);
                }
            }
            return null;
        }
    }


}
