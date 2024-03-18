package com.eischet.janitor.experimental.fresh;

import org.eclipse.collections.api.map.ImmutableMap;

public class FixedPropertyMap implements JPropertyMap {
    private final ImmutableMap<String, JProp> properties;

    public FixedPropertyMap(final ImmutableMap<String, JProp> properties) {
        this.properties = properties;
    }

    @Override
    public Iterable<String> getKeys() {
        return properties.keysView();
    }

    @Override
    public JProp get(final String name) {
        return properties.get(name);
    }

}
