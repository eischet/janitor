package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JBinaryClass {


    /**
     * Encode binary data to base64.
     *
     * @param self the JBinary instance
     * @param process the running script
     * @param arguments arguments,w hich should be empty
     * @return the contents of the JBinary, as a string in base64 format
     */
    public static JString __encodeBase64(final JanitorWrapper<byte[]> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        if (self.janitorGetHostValue() == null) {
            return process.getEnvironment().getBuiltinTypes().emptyString();
        }
        return process.getEnvironment().getBuiltinTypes().string(new String(java.util.Base64.getEncoder().encode(self.janitorGetHostValue())));
    }

    /**
     * Convert the JBinary to a string, like Java's new String(byte[]).
     * Optional first argument: charset name, e.g. UTF-8. If omitted, UTF-8 is assumed.
     *
     * @param self the JBinary
     * @param process the running script
     * @param arguments call arguments
     * @return the string (JString)
     * @throws JanitorRuntimeException on errors
     */
    public static JString __toString(final JanitorWrapper<byte[]> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        String charsetName = arguments.require(0, 1).getOptionalStringValue(0, null);
        try {
            return process.getEnvironment().getBuiltinTypes().string(self.janitorIsTrue() ? new String(self.janitorGetHostValue(), charsetName == null ? StandardCharsets.UTF_8 : Charset.forName(charsetName)) : null);
        } catch (IllegalArgumentException e) {
            throw new JanitorArgumentException(process, "invalid charset: '" + charsetName + "'", e);
        }
    }

    /**
     * Return the size, in bytes, of the JBinary.
     * @param self the JBinary
     * @param process the running script
     * @param arguments call arguments
     * @return the size in bytes
     * @throws JanitorRuntimeException on errors
     */
    public static JInt __size(final JanitorWrapper<byte[]> self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltinTypes().integer(self.janitorGetHostValue().length);
    }

}
