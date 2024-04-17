package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.calls.JNativeMethod;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.json.api.*;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.traits.JAssignable;
import com.eischet.janitor.api.traits.JIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class JMap implements JanitorObject, JIterable, JsonWriter, JsonExportableObject {

    private final Map<JanitorObject, JanitorObject> map = new HashMap<>();
    private final Map<String, JNativeMethod> methods;

    public static JString __toJson(final JMap self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(0);
            return JString.of(self.exportToJson(runningScript.getRuntime().getEnvironment()));
        } catch (JsonException e) {
            throw new JanitorNativeException(runningScript, "error exporting json", e);
        }
    }


    public static JMap __parseJson(final JMap self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return self.parseJson(arguments.require(1).getString(0).janitorGetHostValue(), runningScript.getRuntime().getEnvironment());
        } catch (JsonException e) {
            throw new JanitorNativeException(runningScript, "error parsing json", e);
        }
    }


    public JMap() {
        final Map<String, JNativeMethod> methods = new HashMap<>();
        // methods.put("parseJson", JNativeMethod.of(arguments -> parseJson(arguments.require(1).getString(0).janitorGetHostValue())));

        methods.put("get", JNativeMethod.of(arguments -> get(arguments.require(1).get(0)) ));
        methods.put("__get__", JNativeMethod.of(arguments -> getIndexed(arguments.require(1).get(0)) ));
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

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JNativeMethod localMethod = methods.get(name);
        if (localMethod != null) {
            return localMethod;
        }
        final JString keyString = JString.of(name);
        if (map.containsKey(keyString)) {
            return map.get(keyString);
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    public Set<JanitorObject> keySet() {
        return map.keySet();
    }

    public Collection<JanitorObject> values() {
        return map.values();
    }

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

    public void put(final String key, final JanitorObject value) {
        map.put(JString.of(key), value);
    }

    public void put(final JanitorObject key, final JanitorObject value) {
        map.put(key, value);
    }

    public void put(final @NotNull String key, final @Nullable String value) {
        map.put(JString.of(key), JString.ofNullable(value));
    }

    public JanitorObject get(final JanitorObject key) {
        return JanitorEnvironment.orNull(map.get(key));
    }

    public JanitorObject getIndexed(final JanitorObject key) {
        return TemporaryAssignable.of(get(key), value -> put(key, value));
    }

    @Override
    public String toString() {
        return janitorToString();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void putAll(final JMap map) {
        this.map.putAll(map.map);
    }

    public void applyTo(final JanitorScriptProcess rs, final JanitorObject target) throws JanitorNameException {
        final Set<JanitorObject> notAssignable = new HashSet<>();
        map.forEach((key, value) -> {
            @Nullable final JanitorObject prop = Scope.getOptionalMethod(target, rs, key.janitorToString());
            if (prop instanceof JAssignable assignableProperty) {
                if (!assignableProperty.assign(value)) {
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

    public JMap parseJson(final String json, final JanitorEnvironment env) throws JsonException {
        if (json == null || json.isBlank()) {
            return this;
        }
        final JsonInputStream reader = env.getLenientJsonConsumer(json);
        // final JsonInputStream reader = GsonInputStream.lenient(json);
        return parseJson(reader);
    }

    public JMap parseJson(final JsonInputStream reader) throws JsonException {
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.peek() == JsonTokenType.END_OBJECT) {
                break;
            }
            put(JString.of(reader.nextName()), JCollection.parseJsonValue(reader));
        }
        reader.endObject();
        return this;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "map";
    }

    public void extractString(final JanitorScriptProcess running, final String key, final Consumer<String> consumer) {
        final JanitorObject value = map.get(JString.of(key));
        if (value instanceof JString str) {
            consumer.accept(str.janitorGetHostValue());
        } else if (value != null) {
            running.warn(String.format("extractString for key=%s found %s [%s] where a string was expected", key, value, value.getClass()));
        }
    }

    public void extract(final String key, final Consumer<JanitorObject> consumer) {
        final JanitorObject value = map.get(JString.of(key));
        if (value != null && value != JNull.NULL) {
            consumer.accept(value);
        }
    }

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
                producer.name(pair.getKey().janitorToString());
                ex.writeJson(producer);
            } else if (pair.getValue() instanceof JsonWriter jw) {
                producer.name(pair.getKey().janitorToString());
                jw.writeJson(producer);
            } else {
                throw new JsonException("cannot write " + pair.getValue() + " as json because it does not implement JsonExportable or JsonWriter");
            }
        }
        /*
        for (final Pair<JanitorObject, JanitorObject> pair : map.keyValuesView()) {
            if (pair.getTwo() instanceof JsonExportable ex) {
                if (ex.isDefaultOrEmpty()) {
                    continue;
                }
                producer.name(pair.getOne().janitorToString());
                ex.writeJson(producer);
            } else if (pair.getTwo() instanceof JsonWriter jw) {
                producer.name(pair.getOne().janitorToString());
                jw.writeJson(producer);
            } else {
                throw new JsonException("cannot write " + pair.getTwo() + " as json because it does not implement JsonExportable or JsonWriter");
            }
        }
        */
        producer.endObject();
    }

}
