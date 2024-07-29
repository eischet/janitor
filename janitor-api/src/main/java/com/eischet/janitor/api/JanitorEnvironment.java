package com.eischet.janitor.api;

import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The environment in which Janitor scripts run.
 * This is supposed to be provided by the host application, and is used to interact with the Janitor runtime.
 */
@SuppressWarnings("EmptyMethod")
public interface JanitorEnvironment extends JanitorUserEnvironment, JanitorImplementationEnvironment, JsonOutputSupport {

    /**
     * Helper method: return NULL of the parameter is null, like COALESCE() in SQL.
     * @param obj the object to check
     * @return the object, or NULL if the object is null
     */
    static @NotNull JanitorObject orNull(final @Nullable JanitorObject obj) {
        return obj == null ? JNull.NULL : obj;
    }

    /**
     * Get the formatting object for this environment, for I18N.
     * @return the formatting object
     */
    @NotNull JanitorFormatting getFormatting();

    /**
     * Add a module.
     * @param registration the module's registration, i.g. name plus object provider
     */
    void addModule(final @NotNull JanitorModuleRegistration registration);

    /**
     * Look up an attribute on a class.
     * This was a first step towards easier extension of built-in classes, but should be replaced by dispatch tables.
     *
     * @param runningScript the running script
     * @param instance the object instance
     * @param attributeName the attribute name
     * @return the attribute value, or null if the attribute does not exist
     */
    @Nullable JanitorObject lookupClassAttribute(final @NotNull JanitorScriptProcess runningScript, final @NotNull JanitorObject instance, final @NotNull String attributeName);

    /**
     * Convert a native object to a script object.
     * @param obj the object to convert
     * @return the converted object
     */
    @Nullable JanitorObject nativeToScript(final @Nullable Object obj);

    /**
     * Return a JSON input stream from the JSON text that tries to ignore errors as much as possible.
     * @param json some JSON code
     * @return a JSON input stream
     */
    JsonInputStream getLenientJsonConsumer(final String json);


    /**
     * Create a filter predicate from a script.
     * @param name the name of the script
     * @param code the script code
     * @return the filter predicate
     */
    FilterPredicate filterScript(String name, String code);

    /**
     * Emit a warning message.
     * This may be used by users, but is rather intended for runtimes or scripts to use.
     * @param message the warning message
     */
    void warn(String message);


    /**
     * Access built-in classes like String, Date, etc.
     * @return the built-in classes
     */
    @NotNull JanitorBuiltins getBuiltins();

    /**
     * Return the builtin scope, e.g. for adding thing to it.
     * @return the built-in scope
     */
    Scope getBuiltinScope();


    @NotNull
    JMap parseJsonToMap(final String json) throws JsonException;

    @NotNull
    JList parseJsonToList(final String json) throws JsonException;

}
