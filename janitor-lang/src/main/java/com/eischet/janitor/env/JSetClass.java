package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JBool;
import com.eischet.janitor.api.types.JInt;
import com.eischet.janitor.api.types.JList;
import com.eischet.janitor.api.types.JSet;

public class JSetClass {



    public static JBool __add(final JSet self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.map(self.add(arguments.require(1).get(0)));
    }

    public static JBool __remove(final JSet self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.map(self.remove(arguments.require(1).get(0)));
    }

    public static JBool __contains(final JSet self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.map(self.contains(arguments.require(1).get(0)));
    }

    public static JList __toList(final JSet self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JList.of(self.janitorGetHostValue().stream());
    }

    public static JInt __size(final JSet self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JInt.of(self.size());
    }

    public static JBool __isEmpty(final JSet self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JBool.map(self.janitorGetHostValue().isEmpty());
    }


}
