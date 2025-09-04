package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.toolbox.json.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * A map object, representing a mutable map of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JMap extends JanitorWrapper<Map<JanitorObject, JanitorObject>> implements JIterable, JsonWriter, JsonExportableObject {

    /**
     * Create a new JMap.
     */
    private JMap(final Dispatcher<JanitorWrapper<Map<JanitorObject, JanitorObject>>> dispatch) {
        super(dispatch, new HashMap<>());
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
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorRuntimeException {
        @Nullable final JanitorObject attr = super.janitorGetAttribute(process, name, required);
        if (attr != null) {
            return attr;
        }
        // this is very important for cases where the map is supposed to be used as an implicit object and for map.key access within scripts: return the map key by name
        @NotNull final JString nameAsString = process.getEnvironment().getBuiltinTypes().string(name);
        // return wrapped.get(nameAsString); -- wrong, this returns Java null when the key is missing, but we want Janitor null instead!

        final JanitorObject value = wrapped.get(nameAsString);
        if (value != null) {
            return value;
        }

        if ("missingKey".equals(name)) {
            return null;
            // throw new RuntimeException("missingKey, required = " + required);
        }

        if (required) {
            return JNull.NULL; // required lookup: return "Janitor null" for maps
        } else {
            return null; // not a required lookup: return "Java null" so a caller can walk up to the next scope
        }
    }

    /**
     * Put a key-value pair into the map.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final String key, final JanitorObject value) {
        wrapped.put(Janitor.nullableString(key), value);
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
     * This is a helper that maps Java to Janitor for you.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final @NotNull String key, final @Nullable String value) {
        wrapped.put(Janitor.string(key),  Janitor.nullableString(value));
    }

    /**
     * Put a key-value pair into the map.
     * This is a helper that maps Java to Janitor for you.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final @NotNull String key, final boolean value) {
        wrapped.put(Janitor.string(key), Janitor.toBool(value));
    }

    /**
     * Put a key-value pair into the map.
     * This is a helper that maps Java to Janitor for you.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final @NotNull String key, final int value) {
        wrapped.put(Janitor.string(key), Janitor.integer(value));
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
     * @param process     the script process
     * @param target the target object
     * @throws JanitorNameException if any of the fields could not be assigned
     *                              <p>
     *                                                           TODO: this used to be helpful when Janitor was in a very early stage, but should probably be avoided now
     */
    public void applyTo(final JanitorScriptProcess process, final JanitorObject target) throws JanitorNameException {
        final Set<JanitorObject> notAssignable = new HashSet<>();
        wrapped.forEach((key, value) -> {
            @Nullable final JanitorObject prop = Scope.getOptionalMethod(target, process, key.janitorToString());
            if (prop instanceof JAssignable assignableProperty) {
                try {
                    if (!assignableProperty.assign(value)) {
                        notAssignable.add(key);
                    }
                } catch (JanitorGlueException assignmentError) {
                    // TODO: check if this is really OK
                    process.warn("error assigning to object %s property %s for key %s, value %s -> %s".formatted(target, prop, key, value, assignmentError));
                    notAssignable.add(key);
                }
            } else {
                process.warn("cannot assign to object %s property %s for key %s, value %s".formatted(target, prop, key, value));
                notAssignable.add(key);
            }
        });
        if (!notAssignable.isEmpty()) {
            throw new JanitorNameException(process, "assigned invalid object properties: " + notAssignable + " to instance of class " + simpleClassNameOf(target));
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
     * @param process  script
     * @param key      the key
     * @param consumer the consumer of the value
     */
    public void extractString(final JanitorScriptProcess process, final String key, final Consumer<String> consumer) {
        final JanitorObject value = wrapped.get(Janitor.nullableString(key));
        if (value instanceof JString str) {
            consumer.accept(str.janitorGetHostValue());
        } else if (value != null) {
            process.warn(String.format("extractString for key=%s found %s [%s] where a string was expected", key, value, value.getClass()));
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
        final JanitorObject value = wrapped.get(Janitor.nullableString(key));
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

    public static JMap newInstance(final WrapperDispatchTable<Map<JanitorObject, JanitorObject>> dispatch) {
        return new JMap(dispatch);
    }

    public void readJson(final JsonInputStream stream) throws JsonException {
        stream.beginObject();
        while (stream.hasNext()) {
            final String key = stream.nextKey();
            final JsonTokenType token = stream.peek();
            switch (token) {
                case BEGIN_ARRAY -> {
                    final JList subList = Janitor.list();
                    subList.readJson(stream);
                    put(key, subList);
                }
                case BEGIN_OBJECT -> {
                    final JMap object = Janitor.map();
                    object.readJson(stream);
                }
                case STRING -> put(key, Janitor.string(stream.nextString()));
                case NUMBER -> put(key, Janitor.numeric(stream.nextDouble()));
                case BOOLEAN -> put(key, stream.nextBoolean() ? Janitor.TRUE : Janitor.FALSE);
                case NULL -> put(key, Janitor.NULL);
                case END_ARRAY, END_OBJECT, NAME, END_DOCUMENT -> throw new JsonException("Unexpected token while reading map: " + token);
            }
        }
        stream.endObject();
    }
}
