package com.eischet.janitor.api;

import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.json.api.JsonException;
import com.eischet.janitor.api.json.api.JsonInputStream;
import com.eischet.janitor.api.json.api.JsonWriter;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JanitorEnvironment extends JanitorUserEnvironment, JanitorImplementationEnvironment {

    static JanitorObject orNull(final JanitorObject obj) {
        return obj == null ? JNull.NULL : obj;
    }

    @NotNull JanitorFormatting getFormatting();
    // TODO JanitorObject lookup(String name); onClass
    void addModule(final @NotNull JanitorModuleRegistration registration);

    @Nullable JanitorObject lookupClassAttribute(final @NotNull JanitorScriptProcess runningScript, final @NotNull JanitorObject instance, final @NotNull String attributeName);

    @Nullable JanitorObject nativeToScript(final @Nullable Object obj);

    String writeJson(JsonWriter writer) throws JsonException;

    JsonInputStream getLenientJsonConsumer(final String json);


    FilterPredicate filterScript(String name, String code);

    /**
     * Emit a warning message.
     * This may be used by users, but is rather intended for runtimes or scripts to use.
     * @param message
     */
    void warn(String message);
}
