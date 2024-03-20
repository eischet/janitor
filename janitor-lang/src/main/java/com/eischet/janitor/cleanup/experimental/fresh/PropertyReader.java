package com.eischet.janitor.cleanup.experimental.fresh;

@FunctionalInterface
public interface PropertyReader {
    JObject getValue(JObject instance);
}
