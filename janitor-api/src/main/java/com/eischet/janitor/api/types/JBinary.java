package com.eischet.janitor.api.types;

import com.eischet.janitor.api.scripting.Dispatcher;
import com.eischet.janitor.api.scripting.JanitorWrapper;
import com.eischet.janitor.api.traits.JConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A binary object, like a byte array.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JBinary extends JanitorWrapper<byte[]> implements JConstant {

    public static JBinary newInstance(final Dispatcher<JanitorWrapper<byte[]>> dispatcher, final byte[] wrapped) {
        return new JBinary(dispatcher, wrapped);
    }

    private JBinary(final Dispatcher<JanitorWrapper<byte[]>> dispatcher, final byte[] wrapped) {
        super(dispatcher, wrapped);
    }

    /**
     * Define truthiness: the backing byte array contains a least one byte.
     *
     * @return true if the array is not null and has at least one element
     */
    @Override
    public boolean janitorIsTrue() {
        return wrapped != null && wrapped.length > 0;
    }

    /**
     * Get the size of the binary.
     *
     * @return the size of the binary
     */
    public int size() {
        return wrapped == null ? 0 : wrapped.length;
    }

    @Override
    public byte[] janitorGetHostValue() {
        return wrapped;
    }

    @Override
    public String janitorToString() {
        return Arrays.toString(wrapped);
    }

    @Override
    public String toString() {
        return janitorToString();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "binary";
    }

}
