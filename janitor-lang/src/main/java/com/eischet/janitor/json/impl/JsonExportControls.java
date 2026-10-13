// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.json.impl;

import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;

/**
 * Former export controls, kept only so that existing callers keep compiling.
 * @deprecated use {@link JsonWriteOptions} instead
 */
@Deprecated(forRemoval = true)
public final class JsonExportControls {

    private JsonExportControls() {
    }

    /**
     * @return options that produce indented JSON
     * @deprecated use {@link JsonWriteOptions#PRETTY}
     */
    @Deprecated(forRemoval = true)
    public static JsonWriteOptions pretty() {
        return JsonWriteOptions.PRETTY;
    }

    /**
     * @return options that produce compact JSON
     * @deprecated use {@link JsonWriteOptions#COMPACT}
     */
    @Deprecated(forRemoval = true)
    public static JsonWriteOptions standard() {
        return JsonWriteOptions.COMPACT;
    }

}
