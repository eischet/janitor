package com.eischet.janitor.experimental.fresh;

public interface JPropertyMap {
    Iterable<String> getKeys();
    JProp get(String name);
}
