package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JSet;

import java.util.Set;

/**
 * Operations for Set objects.
 */
public class JSetClass {

    public static JBool __add(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return JBool.of(self.add(arguments.require(1).get(0)));
    }

    public static JBool __remove(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return JBool.of(self.remove(arguments.require(1).get(0)));
    }

    public static JBool __contains(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return JBool.of(self.contains(arguments.require(1).get(0)));
    }

    public static JList __toList(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return runningScript.getEnvironment().getBuiltins().list(self.janitorGetHostValue().stream());
    }

    public static JInt __size(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return runningScript.getEnvironment().getBuiltins().integer(self.size());
    }

    public static JBool __isEmpty(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return JBool.of(self.janitorGetHostValue().isEmpty());
    }

}
