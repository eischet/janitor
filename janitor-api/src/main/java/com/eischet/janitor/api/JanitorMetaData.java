package com.eischet.janitor.api;

import com.eischet.janitor.api.metadata.MetaDataKey;

/**
 * This namespace class contains definitions for common meta-data keys that are used within the Janitor language.
 */
public class JanitorMetaData {

    /**
     * Meta-Data: HELP for an object or property.
     * <p>
     * In Python, they call this a "docstring". Janitor does not currently have syntax to attach such a docstring to
     * an object/property, but it can be provided by the runtime.
     * </p>
     */

    public static final MetaDataKey<String> HELP = new MetaDataKey<>("help", String.class);


    /**
     * Meta-Data: NAME for an object or property.
     * <p>
     * Objects that do have names include functions, methods and classes.
     * For example, "function foo() { ... }" will have the NAME = "foo".
     * </p>
     */
    public static final MetaDataKey<String> NAME = new MetaDataKey<>("name", String.class);

    private JanitorMetaData() {}

}
