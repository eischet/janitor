package com.eischet.janitor.generator;

import org.jetbrains.annotations.NotNull;

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
