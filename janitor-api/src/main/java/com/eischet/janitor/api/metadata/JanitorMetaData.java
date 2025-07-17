package com.eischet.janitor.api.metadata;

public class JanitorMetaData {

    public enum TypeHint {
        NUMBER, INTEGER, FLOAT, STRING, BOOLEAN, METHOD, DATE, DATETIME, LIST
    }

    /**
     * This optional meta-data annotation can be used to tell host code (= Java) what a property's type is supposed to be.
     * This can be useful, for example, when host code wants to write an object into a database.
     * Note that these are <b>Janitor</b> types, not Java types, so e.g. a FLOAT could be a Double in Java or a Float...
     */
    public static final MetaDataKey<TypeHint> TYPE_HINT = new MetaDataKey<>("type_hint", TypeHint.class);

}
