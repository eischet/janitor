package com.eischet.janitor.commons;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;

import java.util.ArrayList;
import java.util.List;

public class OperatingSystemModule extends JanitorComposed<OperatingSystemModule> implements JanitorModule {

    private static final DispatchTable<OperatingSystemModule> dispatcher = new DispatchTable<>();
    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("os", OperatingSystemModule::new);

    static {
        dispatcher.addMethod("exec", OperatingSystemModule::exec);
        dispatcher.addMethod("getenv", OperatingSystemModule::getenv);
    }

    public OperatingSystemModule() {
        super(dispatcher);
    }

    public JanitorObject getenv(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final String name = arguments.getString(0).janitorGetHostValue();
        final String value = System.getenv(name);
        return process.getBuiltins().nullableString(value);
    }

    /**
     * Executes a command in the operating system.
     * @param process the JanitorScriptProcess
     * @param arguments the arguments to the command
     * @return the exit code of the command
     * @throws JanitorRuntimeException on errors
     */
    public JInt exec(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject cmd = arguments.require(1).get(0);
        try {
            if (cmd instanceof JString string) {
                Process osProc = Runtime.getRuntime().exec(string.janitorGetHostValue());
                int result = osProc.waitFor();
                return process.getBuiltins().integer(result);
            } else if (cmd instanceof JList list) {
                final List<String> callArgs = new ArrayList<>();
                for (final JanitorObject element : list) {
                    if (element instanceof JString str) {
                        callArgs.add(str.janitorGetHostValue());
                    } else {
                        callArgs.add(element.janitorToString());
                    }
                }
                final String[] args = callArgs.toArray(new String[0]);
                Process osProc = Runtime.getRuntime().exec(args);
                int result = osProc.waitFor();
                return process.getBuiltins().integer(result);
            } else {
                throw new JanitorArgumentException(process, "invalid arguments: expected string or list, got " + cmd);
            }
        } catch (Exception e) {
            throw new JanitorNativeException(process, "error executing command " + cmd, e);
        }
    }

}
