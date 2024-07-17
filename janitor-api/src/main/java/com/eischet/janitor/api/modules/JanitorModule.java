package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.types.JanitorObject;

/**
 * A JanitorModule is a module that can be imported into a Janitor script.
 * "import foo;", etc.
 */
public interface JanitorModule extends JanitorObject {
    // Right now a module is just an object, there's nothing to implement here.
}
