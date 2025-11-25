package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;

import java.util.Map;

public class JMapClass {

    /**
     * Script method: Convert the map to JSON, which is useful for calling JSON-based APIs from scripts.
     *
     * @param self          the map
     * @param process the script process
     * @param arguments     the arguments
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
     * @param self          the map
     * @param process the script process
     * @param arguments     the arguments
     * @return the map itself
     * @throws JanitorRuntimeException on JSON/runtime errors, e.g. the JSON is not a map but a list
     */
    public static JMap __parseJson(final JanitorWrapper<Map<JanitorObject, JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return parseJson((JMap) self, arguments.require(1).getString(0).janitorGetHostValue(), process.getRuntime().getEnvironment());
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "error parsing json", e);
        }
    }

    public static JanitorObject __get(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        final JanitorObject key = jCallArgs.require(1).get(0);
        return ((JMap) mapJanitorWrapper).get(key);
    }

    public static JanitorObject __getIndexed(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        return ((JMap) mapJanitorWrapper).getIndexed(jCallArgs.require(1).get(0));
    }

    public static JanitorObject __put(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        ((JMap) mapJanitorWrapper).put(jCallArgs.get(0), jCallArgs.get(1));
        return JNull.NULL;
    }

    public static JanitorObject __size(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.integer(mapJanitorWrapper.janitorGetHostValue().size());
    }

    public static JanitorObject __isEmpty(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.toBool(mapJanitorWrapper.janitorGetHostValue().isEmpty());
    }

    public static JanitorObject __keys(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.list(mapJanitorWrapper.janitorGetHostValue().keySet().stream());
    }

    public static JanitorObject __values(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) {
        return Janitor.list(mapJanitorWrapper.janitorGetHostValue().values().stream());
    }

    public static JanitorObject __containsKey(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        return Janitor.toBool(mapJanitorWrapper.janitorGetHostValue().containsKey(jCallArgs.require(1).get(0)));
    }

    public static JanitorObject __containsValue(JanitorWrapper<Map<JanitorObject, JanitorObject>> mapJanitorWrapper, JanitorScriptProcess process, JCallArgs jCallArgs) throws JanitorRuntimeException {
        return Janitor.toBool(mapJanitorWrapper.janitorGetHostValue().containsValue(jCallArgs.require(1).get(0)));
    }

    /**
     * Parse a JSON string into a map.
     *
     * @param json the JSON string
     * @param env  the environment
     * @return the map
     * @throws JsonException on JSON errors
     */
    public static JMap parseJson(final JMap self, final String json, final JanitorEnvironment env) throws JsonException {
        if (json == null || json.isBlank()) {
            return self;
        }
        final JsonInputStream reader = env.getLenientJsonConsumer(json);
        // final JsonInputStream reader = GsonInputStream.lenient(json);
        return parseJson(self, reader, env);
    }

    /**
     * Parse a JSON string into a map.
     *
     * @param reader the JSON reader
     * @return the map
     * @throws JsonException if the JSON is invalid, e.g. it's not really a map
     */
    public static JMap parseJson(final JMap self, final JsonInputStream reader, final JanitorEnvironment env) throws JsonException {
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.peek() == JsonTokenType.END_OBJECT) {
                break;
            }
            self.put(env.getBuiltinTypes().nullableString(reader.nextKey()), JCollection.parseJsonValue(reader, env));
        }
        reader.endObject();
        return self;
    }

}
