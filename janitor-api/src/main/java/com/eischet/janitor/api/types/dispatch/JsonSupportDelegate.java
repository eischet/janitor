// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link JsonSupport} that is assembled from separate reader, writer and default-value check functions.
 * @param <U> the type of the value
 */
public class JsonSupportDelegate<U> implements JsonSupport<U> {

    private final JsonSupportDelegateRead<U> _read;
    private final JsonSupportDelegateWrite<U> _write;
    private final JsonSupportDelegateDefault<U> _default;

    protected JsonSupportDelegate(final JsonSupportDelegateRead<U> read, final JsonSupportDelegateWrite<U> write, final JsonSupportDelegateDefault<U> aDefault) {
        _read = read;
        _write = write;
        _default = aDefault;
    }

    protected JsonSupportDelegate(final JsonSupportDelegateRead<U> read, final JsonSupportDelegateWrite<U> write) {
        _read = read;
        _write = write;
        _default = anything -> false;
    }

    @Override
    public boolean isDefault(@NotNull final U object) {
        return _default.isDefault(object);
    }

    @Override
    public @NotNull U read(final @NotNull JsonInputStream stream) throws JsonException {
        return _read.read(stream);
    }

    @Override
    public void write(final @NotNull JsonOutputStream stream, @NotNull final U object) throws JsonException {
        _write.write(stream, object);
    }
}
