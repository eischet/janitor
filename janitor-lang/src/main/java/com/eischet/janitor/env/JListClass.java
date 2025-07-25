package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Operations for list objects.
 */
public class JListClass {


    public static JList __parseJson(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return parseJson((JList) self, arguments.require(1).getString(0).janitorGetHostValue(), process.getRuntime().getEnvironment());
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "error parsing json", e);
        }
    }

    /**
     * Read a JSON string, representing a list, into this list.
     *
     * @param json the JSON string
     * @param env  the environment
     * @return this list
     * @throws JsonException if the JSON is invalid, e.g. it's not really a list
     */
    public static JList parseJson(final JList self, final String json, final JanitorEnvironment env) throws JsonException {
        if (json == null || json.isBlank()) {
            return self;
        }
        final JsonInputStream reader = env.getLenientJsonConsumer(json);
        return parseJson(self, reader, env);
    }


    /**
     * Read a JSON string, representing a list, into this list.
     *
     * @param reader the JSON reader
     * @return this list
     * @throws JsonException if the JSON is invalid, e.g. it's not really a list
     */
    public static JList parseJson(final JList self, final JsonInputStream reader, final JanitorEnvironment env) throws JsonException {
        reader.beginArray();
        while (reader.hasNext()) {
            self.add(JCollection.parseJsonValue(reader, env));
        }
        reader.endArray();
        return self;
    }


    public static JString __toJson(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(0);
            return process.getEnvironment().getBuiltinTypes().string(((JList) self).exportToJson(process.getRuntime().getEnvironment()));
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "error exporting json", e);
        }
    }

    public static JInt __count(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject countable = arguments.require(1).get(0);
        int count = 0;
        for (final JanitorObject element : self.janitorGetHostValue()) {
            if (Objects.equals(countable, element)) {
                ++count;
            }
        }
        return process.getEnvironment().getBuiltinTypes().integer(count);
    }

    public static JList __filter(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
        if (callable instanceof JCallable func) {
            JList result = process.getEnvironment().getBuiltinTypes().list();
            for (final JanitorObject e : self.janitorGetHostValue()) {
                if (func.call(process, new JCallArgs("filter", process, Collections.singletonList(e))).janitorIsTrue()) {
                    result.add(e);
                }
            }
            return result;
        } else {
            throw new JanitorArgumentException(process, "invalid list::filter parameter: " + callable);
        }
    }

    public static JList __map(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
        if (callable instanceof JCallable func) {
            final JList result = process.getEnvironment().getBuiltinTypes().list(self.janitorGetHostValue().size());
            for (final JanitorObject e : self.janitorGetHostValue()) {
                result.add(func.call(process, new JCallArgs("map", process, Collections.singletonList(e))));
            }
            return result;
        } else {
            throw new JanitorArgumentException(process, "invalid list::map parameter: " + callable);
        }
    }

    public static JString __join(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String separator = arguments.getOptionalStringValue(0, " ");
        return process.getEnvironment().getBuiltinTypes().string(self.janitorGetHostValue().stream().map(JanitorObject::janitorToString).collect(Collectors.joining(separator)));
    }

    public static JList __toList(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltinTypes().list(self.janitorGetHostValue());
    }

    public static JSet __toSet(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltinTypes().set(self.janitorGetHostValue().stream());
    }

    public static JBool __isEmpty(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return Janitor.toBool(self.janitorGetHostValue().isEmpty());
    }

    public static JInt __size(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltinTypes().integer(self.janitorGetHostValue().size());
    }

    public static JBool __contains(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject countable = arguments.require(1).get(0);
        for (final JanitorObject element : self.janitorGetHostValue()) {
            if (Objects.equals(countable, element)) {
                return JBool.TRUE;
            }
        }
        return JBool.FALSE;
    }

    public static JList __randomSublist(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final int count = arguments.getInt(0).getAsInt();
        if (count >= self.janitorGetHostValue().size()) {
            return process.getEnvironment().getBuiltinTypes().list(self.janitorGetHostValue());
        }
        final Random random = new Random();
        final Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < count) {
            indexes.add(random.nextInt(self.janitorGetHostValue().size()));
        }
        final List<JanitorObject> result = new ArrayList<>(indexes.size());
        indexes.forEach(i -> result.add(self.janitorGetHostValue().get(i)));
        return process.getEnvironment().getBuiltinTypes().list(result);
    }

    public static JNull __put(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        ((JList)self).put(arguments.require(1).getInt(0), arguments.get(1));
        return JNull.NULL;
    }

    public static JNull __addAll(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
            ((JList)self).add(jObj);
        }
        return JNull.NULL;
    }

    /*
    TODO: add removeAll
    TODO: make addAll and other *all methods access JIterable instead of JList only
     */

    /*
    public static JList __removAll(final JanitorWrapper<List<JanitorObject>> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject
    }
        */


    public static JNull __sort(final JanitorWrapper<List<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JList self = ((JList) _self);
        arguments.require(0);
        self.replaceAllElements(self.stream().sorted().toList());
        return JNull.NULL;
    }

    public static JNull __add(final JanitorWrapper<List<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JList self = ((JList) _self);
        arguments.require(1, 2);
        if (arguments.size() == 1) {
            self.add(arguments.get(0));
        } else {
            self.add(arguments.getInt(0), arguments.get(1));
        }
        return JNull.NULL;
    }

    public static JanitorObject __get(final JanitorWrapper<List<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JList self = ((JList) _self);
        if (arguments.size() == 1) {
            return self.get(arguments.require(1).getInt(0));
        }
        if (arguments.size() == 2) {
            if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                return self.getRange(process.getEnvironment().getBuiltinTypes().integer(0), process.getEnvironment().getBuiltinTypes().integer(self.size()));
            } else if (arguments.get(0) == JNull.NULL) {
                return self.getRange(process.getEnvironment().getBuiltinTypes().integer(0), arguments.getInt(1));
            } else if (arguments.get(1) == JNull.NULL) {
                return self.getRange(arguments.getInt(0), process.getEnvironment().getBuiltinTypes().integer(self.size()));
            }
            return self.getRange(arguments.getInt(0), arguments.getInt(1));
        }
            /*
            if (arguments.size() == 3) {

            }
             */
        throw new IndexOutOfBoundsException("invalid arguments for get: " + arguments);
    }

    // __get results cannot be assigned to, but __getSliced can be

    public static JanitorObject __getSliced(final JanitorWrapper<List<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JList self = ((JList) _self);
        if (arguments.size() == 1) {
            return self.getIndexed(arguments.require(1).getInt(0));
        }
        // LATER: make ranges assignable, too?
        if (arguments.size() == 2) {
            if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                return self.getRange(process.getEnvironment().getBuiltinTypes().integer(0), process.getEnvironment().getBuiltinTypes().integer(self.size()));
            } else if (arguments.get(0) == JNull.NULL) {
                return self.getRange(process.getEnvironment().getBuiltinTypes().integer(0), arguments.getInt(1));
            } else if (arguments.get(1) == JNull.NULL) {
                return self.getRange(arguments.getInt(0), process.getEnvironment().getBuiltinTypes().integer(self.size()));
            }
            return self.getRange(arguments.getInt(0), arguments.getInt(1));
        }
            /*
            if (arguments.size() == 3) {

            }
             */
        throw new IndexOutOfBoundsException("invalid arguments for get: " + arguments);
    }


    public static JNull __remove(final JanitorWrapper<List<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JList self = ((JList) _self);
        for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
            self.remove(jObj);
        }
        return JNull.NULL;
    }

}
