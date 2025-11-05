package com.eischet.janitor.orm.dao;

/**
 * Tagging interface for a user defined "uplink", which is
 * supposed to be an object representing the whole collection
 * of entity DAOs in your application.
 * <br>
 * Typically, this will have getter methods for the various DAOs
 * that you want to use.
 * <br>
 * We do not specify this any further because we have no way of
 * predefining getter methods for objects we do not know. As a
 * substitute, you will pass functions into the various DAOs that
 * retrieve a Dao&lt;X&gt; from an uplink.
 * <br>
 * We could have omitted this interface but included it so that
 * the &lt;U&gt; parameter that appears in a number of places can
 * lead you to this explanation instead of making you wonder what
 * that's supposed to mean.
 */
public interface Uplink {

}
