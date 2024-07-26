package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorBuiltins;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapper.JanitorWrapper;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * A map object, representing a mutable map of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JMap extends JanitorWrapper<Map<JanitorObject, JanitorObject>> implements JanitorObject, JIterable, JsonWriter, JsonExportableObject {

    private final JanitorBuiltins builtins;

    /**
     * Create a new JMap.
     */
    public JMap(final Dispatcher<JanitorWrapper<Map<JanitorObject, JanitorObject>>> dispatch, JanitorBuiltins builtins) {
        super(dispatch, new HashMap<>());
        this.builtins = builtins;
    }


    /**
     * Get the keys of the map.
     *
     * @return the keys
     */
    public Set<JanitorObject> keySet() {
        return wrapped.keySet();
    }

    /**
     * Get the values of the map.
     *
     * @return the values
     */
    public Collection<JanitorObject> values() {
        return wrapped.values();
    }

    /**
     * Get the size of the map.
     *
     * @return the size
     */
    public int size() {
        return wrapped.size();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return wrapped.keySet().iterator();
    }

    @Override
    public Map<JanitorObject, JanitorObject> janitorGetHostValue() {
        return new HashMap<>(wrapped);
    }

    @Override
    public String janitorToString() {
        return wrapped.toString();
    }

    @Override
    public boolean janitorIsTrue() {
        return !wrapped.isEmpty();
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        @Nullable final JanitorObject attr = super.janitorGetAttribute(runningScript, name, required);
        if (attr != null) {
            return attr;
        }
        // this is very important for cases where the map is supposed to be used as an implicit object and for map.key access within scripts: return the map key by name
        @NotNull final JString nameAsString = runningScript.getEnvironment().getBuiltins().string(name);
        return wrapped.get(nameAsString);
    }

    /**
     * Put a key-value pair into the map.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final String key, final JanitorObject value) {
        wrapped.put(builtins.nullableString(key), value);
    }

    /**
     * Put a key-value pair into the map, builder style.
     *
     * @param key   the key
     * @param value the value
     * @return the same map
     */
    public JMap with(final String key, final JanitorObject value) {
        put(key, value);
        return this;
    }

    /**
     * Put a key-value pair into the map, builder style.
     *
     * @param key   the key
     * @param value the value
     * @return the same map
     */
    public JMap with(final String key, final String value) {
        put(key, value);
        return this;
    }

    /**
     * Put a key-value pair into the map.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final JanitorObject key, final JanitorObject value) {
        wrapped.put(key, value);
    }

    /**
     * Put a key-value pair into the map.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final @NotNull String key, final @Nullable String value) {
        wrapped.put(builtins.nullableString(key),  builtins.nullableString(value));
    }

    /**
     * Get a value from the map.
     *
     * @param key the key
     * @return the value, or NULL if the key is not present or if the associated value IS NULL.
     */
    public JanitorObject get(final JanitorObject key) {
        return JanitorEnvironment.orNull(wrapped.get(key));
    }

    /**
     * Get a value from the map, adressed by an index, for possible assignment to it, e.g. "foo["bar"] = 'baz'".
     *
     * @param key the key
     * @return an assignable object representing an indexed lookup
     */
    public JanitorObject getIndexed(final JanitorObject key) {
        return TemporaryAssignable.of(get(key), value -> put(key, value));
    }

    @Override
    public String toString() {
        return janitorToString();
    }

    /**
     * Check if the map is empty.
     *
     * @return true if the map is empty
     */
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    /**
     * Put all key-value pairs from another map into this map.
     *
     * @param map the other map
     */
    public void putAll(final JMap map) {
        this.wrapped.putAll(map.janitorGetHostValue());
    }

    /**
     * Try to assign all fields of this map to a target object.
     *
     * @param rs     the script process
     * @param target the target object
     * @throws JanitorNameException if any of the fields could not be assigned
     *                              <p>
     *                                                           TODO: this used to be helpful when Janitor was in a very early stage, but should probably be avoided now
     */
    public void applyTo(final JanitorScriptProcess rs, final JanitorObject target) throws JanitorNameException {
        final Set<JanitorObject> notAssignable = new HashSet<>();
        wrapped.forEach((key, value) -> {
            @Nullable final JanitorObject prop = Scope.getOptionalMethod(target, rs, key.janitorToString());
            if (prop instanceof JAssignable assignableProperty) {
                try {
                    if (!assignableProperty.assign(value)) {
                        notAssignable.add(key);
                    }
                } catch (JanitorRuntimeException assignmentError) {
                    // TODO: check if this is really OK
                    rs.warn("error assigning to object %s property %s for key %s, value %s -> %s".formatted(target, prop, key, value, assignmentError));
                    notAssignable.add(key);
                }
            } else {
                rs.warn("cannot assign to object %s property %s for key %s, value %s".formatted(target, prop, key, value));
                notAssignable.add(key);
            }
        });
        if (!notAssignable.isEmpty()) {
            throw new JanitorNameException(rs, "assigned invalid object properties: " + notAssignable);
        }
    }


    @Override
    public @NotNull String janitorClassName() {
        return "map";
    }

    /**
     * Extract a string from the map by key and pass the associated value to the consumer.
     * Do nothing but emit a warning if there's no such key/value.
     *
     * @param running  script
     * @param key      the key
     * @param consumer the consumer of the value
     */
    public void extractString(final JanitorScriptProcess running, final String key, final Consumer<String> consumer) {
        final JanitorObject value = wrapped.get(builtins.nullableString(key));
        if (value instanceof JString str) {
            consumer.accept(str.janitorGetHostValue());
        } else if (value != null) {
            running.warn(String.format("extractString for key=%s found %s [%s] where a string was expected", key, value, value.getClass()));
        }
    }

    /**
     * Extract a value from the map by key and pass it to the consumer.
     * Do nothing if there's not such value or it is NULL.
     *
     * @param key      the key
     * @param consumer the consumer of the value
     */
    public void extract(final String key, final Consumer<JanitorObject> consumer) {
        final JanitorObject value = wrapped.get(builtins.nullableString(key));
        if (value != null && value != JNull.NULL) {
            consumer.accept(value);
        }
    }

    /**
     * Get a value from the map by key, or null if there's no such key.
     *
     * @param key the key
     * @return the value, or null if there's no such key
     */
    public @Nullable JanitorObject getNullable(final JString key) {
        return wrapped.getOrDefault(key, null);
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginObject();

        for (final Map.Entry<JanitorObject, JanitorObject> pair : wrapped.entrySet()) {
            if (pair.getValue() instanceof JsonExportable ex) {
                if (ex.isDefaultOrEmpty()) {
                    continue;
                }
                producer.key(pair.getKey().janitorToString());
                ex.writeJson(producer);
            } else if (pair.getValue() instanceof JsonWriter jw) {
                producer.key(pair.getKey().janitorToString());
                jw.writeJson(producer);
            } else {
                throw new JsonException("cannot write " + pair.getValue() + " as json because it does not implement JsonExportable or JsonWriter");
            }
        }
        producer.endObject();
    }

}
