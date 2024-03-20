package com.eischet.janitor.cleanup.experimental.fresh;

public interface JPropertyMap {
    Iterable<String> getKeys();
    JProp get(String name);
}
