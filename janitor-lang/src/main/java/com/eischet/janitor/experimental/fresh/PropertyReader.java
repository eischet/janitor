package com.eischet.janitor.experimental.fresh;

@FunctionalInterface
public interface PropertyReader {
    JObject getValue(JObject instance);
}
