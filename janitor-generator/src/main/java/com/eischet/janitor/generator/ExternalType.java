// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.generator;

import org.jetbrains.annotations.NotNull;

/** A reference to an existing Java type that is identified by its package and its name. */
public class ExternalType implements JavaType {

    private final @NotNull String packageName;
    private final @NotNull String name;

    public ExternalType(@NotNull final String packageName, @NotNull final String name) {
        this.packageName = packageName;
        this.name = name;
    }

    @Override
    public @NotNull String getPackageName() {
        return packageName;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }


}
