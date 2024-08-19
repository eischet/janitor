package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.modules.ModuleResolver;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputSupport;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The environment in which Janitor scripts run.
 * This is supposed to be provided by the host application, and is used to interact with the Janitor runtime.
 */
@SuppressWarnings("EmptyMethod")
public interface JanitorEnvironment extends JsonOutputSupport {

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
     * Register a module resolver, making it available to script code.
     * Resolvers can be used to load modules dynamically, e.g. from a database or whatever suits the use case.
     * This applies to the import syntax that uses strings: import "module-name" as foo;
     *
     * @param resolver a module resolver
     */
    void addModuleResolver(final ModuleResolver resolver);


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
    JsonInputStream getLenientJsonConsumer(final @Language("JSON") String json);


    /**
     * Create a filter predicate from a script.
     * @param name the name of the script
     * @param code the script code
     * @return the filter predicate
     *
     * TODO: remove this; it uses a badly thought out binding
     */
    @Deprecated
    FilterPredicate filterScript(String name, @Language("Janitor") String code);

    /**
     * Create a filter predicate from a script.
     * The script will be called on every "test" call of the predicate implementation,
     * passing the test parameter as "value" (called "t" in Predicate, which is unfortunate because
     * it makes it hard to talk about).
     *
     * @param name the name of the script
     * @param code the script code
     * @param globalsProvider a callback to bind more script variables
     * @return the filter predicate
     */
    @NotNull FilterPredicate filterScript(@NotNull String name, @NotNull @Language("Janitor") String code, @Nullable final Consumer<Scope> globalsProvider);


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
    @NotNull BuiltinTypes getBuiltinTypes();

    /**
     * Return the builtin scope, e.g. for adding thing to it.
     * @return the built-in scope
     */
    Scope getBuiltinScope();


    @NotNull
    JMap parseJsonToMap(@Language("JSON") final String json) throws JsonException;

    @NotNull
    JList parseJsonToList(@Language("JSON") final String json) throws JsonException;

    @NotNull
    JanitorModule getModuleByQualifier(final JanitorScriptProcess process, String name) throws JanitorRuntimeException;

    @NotNull JanitorModule getModuleByStringName(final JanitorScriptProcess process, String name) throws JanitorRuntimeException;

}
