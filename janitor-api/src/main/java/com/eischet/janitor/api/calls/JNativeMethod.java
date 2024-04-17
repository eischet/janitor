package com.eischet.janitor.api.calls;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.traits.JCallable;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

public class JNativeMethod implements JCallable, JanitorObject {

    @FunctionalInterface
    public
    interface NativeCall {
        JanitorObject execute(JanitorScriptProcess runningScript, JCallArgs arguments) throws Exception;
    }

    @FunctionalInterface
    public interface NativeCallArgsOnly {
        JanitorObject execute(JCallArgs arguments) throws Exception;
    }

    @FunctionalInterface
    public interface NativeCallArgsOnlyVoid {
        void execute(JCallArgs arguments) throws Exception;
    }

    private final String name;
    private final NativeCall nativeCall;

    public JNativeMethod(final NativeCallArgsOnly nativeCall) {
        this.name = null;
        this.nativeCall = (rs, args) -> nativeCall.execute(args);
    }

    public JNativeMethod(final String name, final NativeCallArgsOnly nativeCall) {
        this.name = name;
        this.nativeCall = (rs, args) -> nativeCall.execute(args);
    }

    public JNativeMethod(final NativeCall nativeCall) {
        this.name = null;
        this.nativeCall = nativeCall;
    }

    public JNativeMethod(final String name, final NativeCall nativeCall) {
        this.name = name;
        this.nativeCall = nativeCall;
    }

    public static JNativeMethod of(final String name, final NativeCallArgsOnly code) {
        return new JNativeMethod(name, code);
    }


    public static JNativeMethod of(final NativeCallArgsOnly code) {
        return new JNativeMethod(code);
    }

    public static JNativeMethod ofVoid(final NativeCallArgsOnlyVoid code) {
        return new JNativeMethod(arguments -> {
            code.execute(arguments);
            return JNull.NULL;
        });
    }

    @Override
    public JanitorObject call(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final JanitorObject res = nativeCall.execute(runningScript, arguments);
            if (res != null) {
                return res;
            }
            return JNull.NULL;
        } catch (Exception e) {
            if (e instanceof JanitorRuntimeException rte) {
                throw rte;
            } else {
                throw new JanitorNativeException(runningScript, "%s: error calling native code".formatted(arguments.getFunctionName()), e);
            }
        }
    }

    @Override
    public @NotNull String janitorClassName() {
        return "function";
    }

    @Override
    public String janitorToString() {
        if (name == null) {
            return "[native function]";
        }
        return "[native function " + name + "]";
    }

    public String getName() {
        return name;
    }
}
