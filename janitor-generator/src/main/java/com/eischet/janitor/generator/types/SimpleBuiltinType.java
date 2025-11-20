package com.eischet.janitor.generator.types;

import com.eischet.janitor.generator.JavaType;
import org.jetbrains.annotations.NotNull;

public enum SimpleBuiltinType implements JavaType {
    STRING(String.class),
    LONG(Long.class),
    INTEGER(Integer.class),
    BOOLEAN(Boolean.class),
    DOUBLE(Double.class),
    FLOAT(Float.class),
    BYTE(Byte.class),
    SHORT(Short.class),
    CHAR(Character.class),
    OBJECT(Object.class);

    private final @NotNull String packageName;
    private final @NotNull String name;

    SimpleBuiltinType(final Class<?> typeClass) {
        this.packageName = typeClass.getPackageName();
        this.name = typeClass.getSimpleName();
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
