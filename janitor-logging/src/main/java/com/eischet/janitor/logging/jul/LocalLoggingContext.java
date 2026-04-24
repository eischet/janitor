package com.eischet.janitor.logging.jul;

import org.jetbrains.annotations.Nullable;

public class LocalLoggingContext implements ILoggingContext {
    private @Nullable String app;
    private @Nullable String user;
    private @Nullable String entity;

    public @Nullable String getApp() {
        return app;
    }

    public LocalLoggingContext setApp(final @Nullable String app) {
        this.app = app;
        return this;
    }

    @Override
    public @Nullable String getUser() {
        return user;
    }

    public LocalLoggingContext setUser(final @Nullable String user) {
        this.user = user;
        return this;
    }

    public @Nullable String getEntity() {
        return entity;
    }

    public LocalLoggingContext setEntity(final @Nullable String entity) {
        this.entity = entity;
        return this;
    }

}
