package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.calls.JNativeMethod;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.traits.JAssignable;
import com.eischet.janitor.api.traits.JIterable;
import com.eischet.janitor.toolbox.json.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * A map object, representing a mutable map of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JMap implements JanitorObject, JIterable, JsonWriter, JsonExportableObject {

    private final Map<JanitorObject, JanitorObject> map = new HashMap<>();
    private final Map<String, JNativeMethod> methods;

    // TODO: all the __implementation methods should be moved to janitor-lang, into a dispatch table

    /**
     * Create a new JMap.
     */
    public JMap() {
        final Map<String, JNativeMethod> methods = new HashMap<>();
        // methods.put("parseJson", JNativeMethod.of(arguments -> parseJson(arguments.require(1).getString(0).janitorGetHostValue())));

        // TODO: move all these to a proper dispatch table
        methods.put("get", JNativeMethod.of(arguments -> get(arguments.require(1).get(0))));
        methods.put("__get__", JNativeMethod.of(arguments -> getIndexed(arguments.require(1).get(0))));
        methods.put("put", JNativeMethod.ofVoid(arguments -> put(arguments.require(2).get(0), arguments.get(1))));
        methods.put("size", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JInt.of(map.size());
        }));
        methods.put("isEmpty", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JBool.map(map.isEmpty());
        }));
        methods.put("keys", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return new JList(map.keySet().stream());
        }));
        methods.put("values", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return new JList(map.values().stream());
        }));
        /*
        methods.put("toJson", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JString.of(exportToJson(runningScript.getRuntime().getEnvironment()));
        }));
         */
        this.methods = methods;
    }

    /**
     * Script method: Convert the map to JSON, which is useful for calling JSON-based APIs from scripts.
     *
     * @param self          the map
     * @param runningScript the script process
     * @param arguments     the arguments
     * @return the JSON string
     * @throws JanitorRuntimeException on errors
     */
    public static JString __toJson(final JMap self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(0);
            return runningScript.getEnvironment().getBuiltins().string(self.exportToJson(runningScript.getRuntime().getEnvironment()));
        } catch (JsonException e) {
            throw new JanitorNativeException(runningScript, "error exporting json", e);
        }
    }

    /**
     * Script method: Parse a JSON string into an existing map.
     *
     * @param self          the map
     * @param runningScript the script process
     * @param arguments     the arguments
     * @return the map itself
     * @throws JanitorRuntimeException on JSON/runtime errors, e.g. the JSON is not a map but a list
     */
    public static JMap __parseJson(final JMap self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return self.parseJson(arguments.require(1).getString(0).janitorGetHostValue(), runningScript.getRuntime().getEnvironment());
        } catch (JsonException e) {
            throw new JanitorNativeException(runningScript, "error parsing json", e);
        }
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JNativeMethod localMethod = methods.get(name);
        if (localMethod != null) {
            return localMethod;
        }
        final JString keyString = runningScript.getEnvironment().getBuiltins().string(name);
        if (map.containsKey(keyString)) {
            return map.get(keyString);
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    /**
     * Get the keys of the map.
     *
     * @return the keys
     */
    public Set<JanitorObject> keySet() {
        return map.keySet();
    }

    /**
     * Get the values of the map.
     *
     * @return the values
     */
    public Collection<JanitorObject> values() {
        return map.values();
    }

    /**
     * Get the size of the map.
     *
     * @return the size
     */
    public int size() {
        return map.size();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return map.keySet().iterator();
    }

    @Override
    public Map<JanitorObject, JanitorObject> janitorGetHostValue() {
        return map;
    }

    @Override
    public String janitorToString() {
        return map.toString();
    }

    @Override
    public boolean janitorIsTrue() {
        return !map.isEmpty();
    }

    /**
     * Put a key-value pair into the map.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final String key, final JanitorObject value) {
        map.put(JString.of(key), value);
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
        map.put(key, value);
    }

    /**
     * Put a key-value pair into the map.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final @NotNull String key, final @Nullable String value) {
        map.put(JString.of(key), JString.ofNullable(value));
    }

    /**
     * Get a value from the map.
     *
     * @param key the key
     * @return the value, or NULL if the key is not present or if the associated value IS NULL.
     */
    public JanitorObject get(final JanitorObject key) {
        return JanitorEnvironment.orNull(map.get(key));
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
        return map.isEmpty();
    }

    /**
     * Put all key-value pairs from another map into this map.
     *
     * @param map the other map
     */
    public void putAll(final JMap map) {
        this.map.putAll(map.map);
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
        map.forEach((key, value) -> {
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

    /**
     * Parse a JSON string into a map.
     *
     * @param json the JSON string
     * @param env  the environment
     * @return the map
     * @throws JsonException on JSON errors
     */
    public JMap parseJson(final String json, final JanitorEnvironment env) throws JsonException {
        if (json == null || json.isBlank()) {
            return this;
        }
        final JsonInputStream reader = env.getLenientJsonConsumer(json);
        // final JsonInputStream reader = GsonInputStream.lenient(json);
        return parseJson(reader);
    }

    /**
     * Parse a JSON string into a map.
     *
     * @param reader the JSON reader
     * @return the map
     * @throws JsonException if the JSON is invalid, e.g. it's not really a map
     */
    public JMap parseJson(final JsonInputStream reader) throws JsonException {
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.peek() == JsonTokenType.END_OBJECT) {
                break;
            }
            put(JString.of(reader.nextKey()), JCollection.parseJsonValue(reader));
        }
        reader.endObject();
        return this;
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
        final JanitorObject value = map.get(JString.of(key));
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
        final JanitorObject value = map.get(JString.of(key));
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
        return map.getOrDefault(key, null);
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return map.isEmpty();
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginObject();

        for (final Map.Entry<JanitorObject, JanitorObject> pair : map.entrySet()) {
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
