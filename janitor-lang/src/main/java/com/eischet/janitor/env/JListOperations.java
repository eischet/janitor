package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.api.traits.JCallable;
import com.eischet.janitor.api.types.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Operations for list objects.
 * TODO: wrap these with a DispatchTable, to allow greater customisation options to hosts.
 */
public class JListOperations {


    public static JList __parseJson(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            return self.parseJson(arguments.require(1).getString(0).janitorGetHostValue(), runningScript.getRuntime().getEnvironment());
        } catch (JsonException e) {
            throw new JanitorNativeException(runningScript, "error parsing json", e);
        }
    }

    public static JString __toJson(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(0);
            return runningScript.getRuntime().string(self.exportToJson(runningScript.getRuntime().getEnvironment()));
        } catch (JsonException e) {
            throw new JanitorNativeException(runningScript, "error exporting json", e);
        }
    }

    public static JInt __count(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject countable = arguments.require(1).get(0);
        int count = 0;
        for (final JanitorObject element : self) {
            if (Objects.equals(countable, element)) {
                ++count;
            }
        }
        return JInt.of(count);
    }

    public static JList __filter(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
        if (callable instanceof JCallable func) {
            JList result = new JList();
            for (final JanitorObject e : self) {
                if (func.call(runningScript, new JCallArgs("filter", runningScript, Collections.singletonList(e))).janitorIsTrue()) {
                    result.add(e);
                }
            }
            return result;
        } else {
            throw new JanitorArgumentException(runningScript, "invalid list::filter parameter: " + callable);
        }
    }

    public static JList __map(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
        if (callable instanceof JCallable func) {
            final JList result = new JList(self.size());
            for (final JanitorObject e : self) {
                result.add(func.call(runningScript, new JCallArgs("map", runningScript, Collections.singletonList(e))));
            }
            return result;
        } else {
            throw new JanitorArgumentException(runningScript, "invalid list::map parameter: " + callable);
        }
    }

    public static JString __join(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String separator = arguments.getOptionalStringValue(0, " ");
        return runningScript.getEnvironment().string(self.stream().map(JanitorObject::janitorToString).collect(Collectors.joining(separator)));
    }

    public static JSet __toSet(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return new JSet(self.stream());
    }

    public static JBool __isEmpty(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JBool.map(self.isEmpty());
    }

    public static JInt __size(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JInt.of(self.size());
    }

    public static JBool __contains(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject countable = arguments.require(1).get(0);
        for (final JanitorObject element : self) {
            if (Objects.equals(countable, element)) {
                return JBool.TRUE;
            }
        }
        return JBool.FALSE;
    }

    public static JList __randomSublist(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final int count = arguments.getInt(0).getAsInt();
        if (count >= self.size()) {
            return new JList(self);
        }
        final Random random = new Random();
        final Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < count) {
            indexes.add(random.nextInt(self.size()));
        }
        final List<JanitorObject> result = new ArrayList<>(indexes.size());
        indexes.forEach(i -> result.add(self.get(i)));
        return new JList(result);
    }

    public static JNull __put(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        self.put(arguments.require(1).getInt(0), arguments.get(1));
        return JNull.NULL;
    }

    public static JNull __addAll(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
            self.add(jObj);
        }
        return JNull.NULL;
    }

    public static JNull __add(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1, 2);
        if (arguments.size() == 1) {
            self.add(arguments.get(0));
        } else {
            self.add(arguments.getInt(0), arguments.get(1));
        }
        return JNull.NULL;
    }

    public static JanitorObject __get(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        if (arguments.size() == 1) {
            return self.get(arguments.require(1).getInt(0));
        }
        if (arguments.size() == 2) {
            if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                return self.getRange(JInt.of(0), JInt.of(self.size()));
            } else if (arguments.get(0) == JNull.NULL) {
                return self.getRange(JInt.of(0), arguments.getInt(1));
            } else if (arguments.get(1) == JNull.NULL) {
                return self.getRange(arguments.getInt(0), JInt.of(self.size()));
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

    public static JanitorObject __getSliced(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        if (arguments.size() == 1) {
            return self.getIndexed(arguments.require(1).getInt(0));
        }
        // LATER: make ranges assignable, too?
        if (arguments.size() == 2) {
            if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                return self.getRange(JInt.of(0), JInt.of(self.size()));
            } else if (arguments.get(0) == JNull.NULL) {
                return self.getRange(JInt.of(0), arguments.getInt(1));
            } else if (arguments.get(1) == JNull.NULL) {
                return self.getRange(arguments.getInt(0), JInt.of(self.size()));
            }
            return self.getRange(arguments.getInt(0), arguments.getInt(1));
        }
            /*
            if (arguments.size() == 3) {

            }
             */
        throw new IndexOutOfBoundsException("invalid arguments for get: " + arguments);
    }


    public static JNull __remove(final JList self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
            self.remove(jObj);
        }
        return JNull.NULL;
    }

}
