package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JSet;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;

import java.util.Set;

/**
 * Operations for Set objects.
 */
public class JSetClass {

    public static JBool __add(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return Janitor.toBool(self.add(arguments.require(1).get(0)));
    }

    public static JBool __remove(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return Janitor.toBool(self.remove(arguments.require(1).get(0)));
    }

    public static JBool __contains(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        return Janitor.toBool(self.contains(arguments.require(1).get(0)));
    }

    public static JSet __toSet(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return process.getBuiltins().set(self.janitorGetHostValue().stream());
    }

    public static JList __toList(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return process.getBuiltins().list(self.janitorGetHostValue().stream());
    }

    public static JInt __size(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return process.getBuiltins().integer(self.size());
    }

    public static JBool __isEmpty(final JanitorWrapper<Set<JanitorObject>> _self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JSet self = ((JSet) _self);
        arguments.require(0);
        return Janitor.toBool(self.janitorGetHostValue().isEmpty());
    }

}
