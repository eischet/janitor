package com.eischet.janitor.cleanup.runtime.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JNull;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.tools.JanitorConverter;
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

    private final NativeCall nativeCall;

    public JNativeMethod(final NativeCallArgsOnly nativeCall) {
        this.nativeCall = (rs, args) -> nativeCall.execute(args);
    }

    public JNativeMethod(final NativeCall nativeCall) {
        this.nativeCall = nativeCall;
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
            return JanitorConverter.orNull(nativeCall.execute(runningScript, arguments));
        } catch (Exception e) {
            if (e instanceof JanitorRuntimeException rte) {
                throw rte;
            } else {
                throw new JanitorNativeException(runningScript, "%s: error calling native code".formatted(arguments.getFunctionName()), e);
            }
        }
    }

    @Override
    public Object janitorGetHostValue() {
        return this;
    }

    @Override
    public String janitorToString() {
        return toString();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "function";
    }

}
