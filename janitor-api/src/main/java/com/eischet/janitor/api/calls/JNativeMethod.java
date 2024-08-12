package com.eischet.janitor.api.calls;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JCallable;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A convenience class for making Java code accessible to the scripting language.
 */
public class JNativeMethod implements JCallable, JanitorObject {

    /**
     * A functional interface for a native call, which your app will implement.
     * This variant makes use of the running script, in order to be able to pass it to other Janitor code.
     * @see NativeCallArgsOnly
     * @see NativeCallArgsOnlyVoid
     */
    @FunctionalInterface
    public interface NativeCall {
        JanitorObject execute(final JanitorScriptProcess process, final JCallArgs arguments) throws Exception;
    }

    /**
     * A functional interface for a native call, which your app will implement.
     * This variant does not use the running script, so use this when access to other Janitor code is not required.
     *
     * @see NativeCall
     * @see NativeCallArgsOnlyVoid
     */
    @FunctionalInterface
    public interface NativeCallArgsOnly {
        JanitorObject execute(final JCallArgs arguments) throws Exception;
    }

    /**
     * A functional interface for a native call that returns void (nothing), which your app will implement.
     * This does not use the running script, so use this when access to other Janitor code is not required.
     * @see NativeCall
     * @see NativeCallArgsOnly
     */
    @FunctionalInterface
    public interface NativeCallArgsOnlyVoid {
        void execute(final JCallArgs arguments) throws Exception;
    }


    private final String name;
    private final NativeCall nativeCall;

    /**
     * Create a new native method.
     * @param nativeCall the native call to execute.
     */
    public JNativeMethod(final NativeCallArgsOnly nativeCall) {
        this.name = null;
        this.nativeCall = (process, args) -> nativeCall.execute(args);
    }

    /**
     * Create a new native method.
     * @param nativeCall the native call to execute.
     */
    public JNativeMethod(final String name, final NativeCallArgsOnly nativeCall) {
        this.name = name;
        this.nativeCall = (process, args) -> nativeCall.execute(args);
    }

    /**
     * Create a new native method.
     * @param nativeCall the native call to execute.
     */
    public JNativeMethod(final NativeCall nativeCall) {
        this.name = null;
        this.nativeCall = nativeCall;
    }

    /**
     * Create a new native method and give it a name.
     * @param name the name of the method.
     * @param nativeCall the native call to execute.
     */
    public JNativeMethod(final String name, final NativeCall nativeCall) {
        this.name = name;
        this.nativeCall = nativeCall;
    }

    /**
     * Create a new native method and give it a name.
     * @param name the name of the method.
     * @param code the native call to execute.
     * @return a new native method.
     */
    public static JNativeMethod of(final String name, final NativeCallArgsOnly code) {
        return new JNativeMethod(name, code);
    }


    /**
     * Create a new native method and give it a name.
     * @param code the native call to execute.
     * @return a new native method.
     */
    public static JNativeMethod of(final NativeCallArgsOnly code) {
        return new JNativeMethod(code);
    }

    /**
     * Create a new native method and give it a name.
     * @param code the native call to execute.
     * @return a new native method.
     */
    public static JNativeMethod ofVoid(final NativeCallArgsOnlyVoid code) {
        return new JNativeMethod(arguments -> {
            code.execute(arguments);
            return JNull.NULL;
        });
    }

    /**
     * Call the method.
     * The interpreter will do this for you, don't call this yourself.
     *
     * @param process the script that is running.
     * @param arguments the arguments to pass to the method.
     * @return the result of the method's execution.
     * @throws JanitorRuntimeException when runtime errors occur
     */
    @Override
    public JanitorObject call(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final JanitorObject res = nativeCall.execute(process, arguments);
            if (res != null) {
                return res;
            }
            return JNull.NULL;
        } catch (Throwable e) { // used to be Exception, but Throwable makes much more sense since we want the have this in the script, not kill the script!
            if (e instanceof JanitorRuntimeException rte) {
                throw rte;
            } else {
                throw new JanitorNativeException(process, "%s: error calling native code".formatted(arguments.getFunctionName()), e);
            }
        }
    }

    @Override
    public @NotNull String janitorClassName() {
        return "Function";
    }

    @Override
    public String janitorToString() {
        if (name == null) {
            return "[native function]";
        }
        return "[native function " + name + "]";
    }

    /**
     * Get the name of the method.
     * @return the name of the method
     */
    public @Nullable String getName() {
        return name;
    }
}
