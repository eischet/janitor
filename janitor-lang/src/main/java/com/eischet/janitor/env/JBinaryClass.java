package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JString;

public class JBinaryClass {


    public static JString __encodeBase64(final JanitorWrapper<byte[]> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) {
        if (self.janitorGetHostValue() == null) {
            return runningScript.getEnvironment().getBuiltins().emptyString();
        }
        return runningScript.getEnvironment().getBuiltins().string(new String(java.util.Base64.getEncoder().encode(self.janitorGetHostValue())));
    }

    public static JString __toString(final JanitorWrapper<byte[]> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) {
        return runningScript.getEnvironment().getBuiltins().string(self.janitorIsTrue() ? new String(self.janitorGetHostValue()) : null);
    }

    public static JInt __size(final JanitorWrapper<byte[]> self, final JanitorScriptProcess runningScript, final JCallArgs arguments) {
        return runningScript.getEnvironment().getBuiltins().integer(self.janitorGetHostValue().length);
    }

}
