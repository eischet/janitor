// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.impl;

import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;

/**
 * Creates JSON streams that need no library: {@link NativeInputStream} and {@link NativeOutputStream}.
 * Select it with {@code -Djanitor.json=native} or {@link JsonStreamFactory#named(String)}.
 */
public class NativeJsonStreams implements JsonStreamFactory {

    /** The name of this implementation. */
    public static final String NAME = "native";

    @Override
    public @NotNull String name() {
        return NAME;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public @NotNull JsonInputStream reader(final @NotNull String json, final @NotNull JsonReadMode mode) {
        return new NativeInputStream(json, mode);
    }

    @Override
    public @NotNull JsonInputStream reader(final @NotNull Reader source, final @NotNull JsonReadMode mode) {
        return new NativeInputStream(source, mode);
    }

    @Override
    public @NotNull JsonOutputStream writer(final @NotNull JsonWriteOptions options, final @NotNull Writer target) {
        return new NativeOutputStream(options, target);
    }

    @Override
    public String toString() {
        return NAME;
    }

}
