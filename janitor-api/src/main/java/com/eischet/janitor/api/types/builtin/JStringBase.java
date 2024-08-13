package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.JanitorTypedObject;

/**
 * Experimental: trying to establish JString as this intermediate interface.
 * The idea here is to replace JString the class with JString the interface and move the class
 * into the -lang package, leaving only an interface in the public API. I'm not sure if that's really
 * worth the effort.
 */
public interface JStringBase extends JanitorTypedObject<String> {
}
