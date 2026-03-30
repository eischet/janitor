package com.eischet.janitor.modules.httpclient;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.modules.JanitorNativeModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.lang.JNativeMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpClientModule extends JanitorNativeModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("httpclient", HttpClientModule::new);

    private final JanitorHttpClient client;
    private final JNativeMethod fetch;
    private final JNativeMethod build;

    public HttpClientModule() {
        this.client = new JanitorHttpClient();
        this.fetch = new JNativeMethod((runningScript, arguments) -> Janitor.nullableString(client.getString(arguments.getString(0).janitorGetHostValue())));
        this.build = new JNativeMethod(((runningScript, arguments) -> new JanitorHttpClient()));
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) {
        if ("fetch".equals(name)) {
            return fetch;
        }
        if ("build".equals(name)) {
            return build;
        }
        return null;
    }


}