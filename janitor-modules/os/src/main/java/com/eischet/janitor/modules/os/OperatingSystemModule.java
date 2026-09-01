package com.eischet.janitor.modules.os;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Operating-system access for scripts: run arbitrary commands, read arbitrary environment variables.
 * <p>
 * This module is deliberately unrestricted, same as {@code files} (see
 * {@code com.eischet.janitor.modules.files.FilesModule} for the full rationale: other embedded/polyglot
 * runtimes gate whole capabilities behind explicit opt-in rather than restricting what a granted
 * capability can reach). It is privileged and only ever available to a script if the host application
 * explicitly registers it (never auto-registered); the host is responsible for not registering it for
 * untrusted scripts.
 */
public class OperatingSystemModule extends JanitorComposed<OperatingSystemModule> implements JanitorModule {

    private static final DispatchTable<OperatingSystemModule> dispatcher = new DispatchTable<>(OperatingSystemModule::new, false);
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
                final Process osProc = Runtime.getRuntime().exec(string.janitorGetHostValue());
                return waitFor(process, osProc);
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
                final Process osProc = Runtime.getRuntime().exec(args);
                return waitFor(process, osProc);
            } else {
                throw new JanitorArgumentException(process, "invalid arguments: expected string or list, got " + cmd);
            }
        } catch (Exception e) {
            throw new JanitorNativeException(process, "error executing command " + cmd, e);
        }
    }

    /**
     * Wait for a child process to finish, while draining its stdout/stderr in the background so
     * waitFor() can't deadlock: a child that writes more than the OS pipe buffer (~64 KB) to either
     * stream blocks on the write until someone reads it, and nothing here ever reads it otherwise --
     * we only care about the exit code, not the output.
     */
    private static JInt waitFor(final JanitorScriptProcess process, final Process osProc) throws InterruptedException {
        drainInBackground(osProc.getInputStream());
        drainInBackground(osProc.getErrorStream());
        final int result = osProc.waitFor();
        return process.getBuiltins().integer(result);
    }

    private static void drainInBackground(final InputStream stream) {
        final Thread drain = new Thread(() -> {
            try (stream) {
                stream.readAllBytes();
            } catch (IOException ignored) {
                // The process may have already exited, or the stream got closed concurrently --
                // either way there's nothing left to drain and nothing to act on here.
            }
        });
        drain.setDaemon(true);
        drain.start();
    }

}
