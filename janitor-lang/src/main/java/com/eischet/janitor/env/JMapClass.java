// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;
import org.intellij.lang.annotations.Language;

import java.util.Map;

/** Operations for map objects. */
public class JMapClass {

    /**
     * Script method: Convert the map to JSON, which is useful for calling JSON-based APIs from scripts.
     *
     * @param self      the map
     * @param process   the script process
     * @param arguments the arguments
     * @return the JSON string
     * @throws JanitorRuntimeException on errors
     */
    public static JString __toJson(final JanitorWrapper<Map<JanitorObject, JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(0);
            return Janitor.string(((JMap) self).exportToJson(process.getEnvironment()));
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "error exporting json", e);
        }
    }

    /**
     * Script method: Parse a JSON string into an existing map.
     *
     * @param self      the map
     * @param process   the script process
     * @param arguments the arguments
     * @return the map itself
     * @throws JanitorRuntimeException on JSON/runtime errors, e.g. the JSON is not a map but a list
     */
    public static JMap __parseJson(final JanitorWrapper<Map<JanitorObject, JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return parseJson((JMap) self, arguments.require(1).getString(0).janitorGetHostValue());
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "error parsing json", e);
        }
    }

    /**
     * Script method {@code map.get(key)}: gets the value for a key.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return the value, or null if there is none
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject __get(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        final JanitorObject key = jCallArgs.require(1).get(0);
        return ((JMap) mapJanitorWrapper).get(key);
    }

    /**
     * Script method for indexing, e.g. {@code map[key]}: gets the value for a key, in a form that can be assigned to.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return the value
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject __getIndexed(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        return ((JMap) mapJanitorWrapper).getIndexed(jCallArgs.require(1).get(0));
    }

    /**
     * Script method {@code map.put(key, value)}: stores a value under a key.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return null
     */
    public static JanitorObject __put(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        ((JMap) mapJanitorWrapper).put(jCallArgs.get(0), jCallArgs.get(1));
        return JNull.NULL;
    }

    /**
     * Script method {@code map.size()}: the number of entries in this map.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return the number of entries
     */
    public static JanitorObject __size(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.integer(mapJanitorWrapper.janitorGetHostValue().size());
    }

    /**
     * Script method {@code map.isEmpty()}: checks whether this map has no entries.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return true if the map is empty
     */
    public static JanitorObject __isEmpty(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.toBool(mapJanitorWrapper.janitorGetHostValue().isEmpty());
    }

    /**
     * Script method {@code map.keys()}: the keys of this map.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return a list of the keys
     */
    public static JanitorObject __keys(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.list(mapJanitorWrapper.janitorGetHostValue().keySet().stream());
    }

    /**
     * Script method {@code map.values()}: the values of this map.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return a list of the values
     */
    public static JanitorObject __values(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.list(mapJanitorWrapper.janitorGetHostValue().values().stream());
    }

    /**
     * Script method {@code map.containsKey(key)}: checks whether this map has an entry for the key.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return true if the key was found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject __containsKey(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        return Janitor.toBool(mapJanitorWrapper.janitorGetHostValue().containsKey(jCallArgs.require(1).get(0)));
    }

    /**
     * Script method {@code map.containsValue(value)}: checks whether this map has an entry with the value.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param jCallArgs the call arguments
     * @return true if the value was found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject __containsValue(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        return Janitor.toBool(mapJanitorWrapper.janitorGetHostValue().containsValue(jCallArgs.require(1).get(0)));
    }

    /**
     * Creates a shallow copy of the map.
     *
     * @param self    map
     * @param process process
     * @param args    args, must be empty
     * @return a shallow copy of the map
     * @throws JanitorRuntimeException on runtime errors
     */
    public static JanitorObject __copy(JanitorWrapper<Map<JanitorObject, JanitorObject>> self, JanitorScriptProcess process, JCallArgs args) throws JanitorRuntimeException {
        args.notAllowed();
        final var copy = Janitor.map();
        self.janitorGetHostValue().forEach(copy::put);
        return copy;
    }

    /**
     * Parse a JSON string into a map.
     *
     * @param json the JSON string
     * @return the map
     * @throws JsonException on JSON errors
     */
    public static JMap parseJson(final JMap self, @Language("JSON") final String json) throws JsonException {
        if (json == null || json.isBlank()) {
            return self;
        }
        final JsonInputStream reader = Janitor.current().getLenientJsonConsumer(json);
        // final JsonInputStream reader = GsonInputStream.lenient(json);
        return parseJson(self, reader);
    }

    /**
     * Parse a JSON string into a map.
     *
     * @param reader the JSON reader
     * @return the map
     * @throws JsonException if the JSON is invalid, e.g. it's not really a map
     */
    public static JMap parseJson(final JMap self, final JsonInputStream reader) throws JsonException {
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.peek() == JsonTokenType.END_OBJECT) {
                break;
            }
            self.put(Janitor.nullableString(reader.nextKey()), JCollection.parseJsonValue(reader));
        }
        reader.endObject();
        return self;
    }

    /**
     * Script method {@code map.clear()}: removes all entries from this map.
     * @param mapJanitorWrapper the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __clear(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        mapJanitorWrapper.janitorGetHostValue().clear();
        return JNull.NULL;
    }

    /**
     * Registers the standard methods and properties of maps.
     * @param mapDispatcher the dispatch table to add them to
     */
    public static void applyDefaults(WrapperDispatchTable<Map<JanitorObject, JanitorObject>> mapDispatcher) {
        mapDispatcher.addMethod("toJson", JMapClass::__toJson);
        mapDispatcher.addMethod("parseJson", JMapClass::__parseJson);
        mapDispatcher.addMethod("get", JMapClass::__get);
        mapDispatcher.addMethod(JanitorAntlrCompiler.INDEXED_GET_METHOD, JMapClass::__getIndexed);
        mapDispatcher.addMethod("put", JMapClass::__put);
        mapDispatcher.addMethod("size", JMapClass::__size);
        mapDispatcher.addMethod("isEmpty", JMapClass::__isEmpty);
        mapDispatcher.addMethod("keys", JMapClass::__keys);
        mapDispatcher.addMethod("values", JMapClass::__values);
        mapDispatcher.addMethod("containsKey", JMapClass::__containsKey);
        mapDispatcher.addMethod("containsValue", JMapClass::__containsValue);
        mapDispatcher.addMethod("clear", JMapClass::__clear);
        mapDispatcher.addMethod("copy", JMapClass::__copy);
    }
}
