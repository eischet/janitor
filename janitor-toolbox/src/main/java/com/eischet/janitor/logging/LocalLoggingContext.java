package com.eischet.janitor.logging;

import com.eischet.janitor.logging.ILoggingContext;
import org.jetbrains.annotations.Nullable;

/** A mutable logging context with the application, user and entity that the current thread is working for. */
public class LocalLoggingContext implements ILoggingContext {
    private @Nullable String app;
    private @Nullable String user;
    private @Nullable String entity;

    /**
     * @return the application name, or null
     */
    public @Nullable String getApp() {
        return app;
    }

    /**
     * Sets the application.
     * @param app the application name, or null
     * @return this context
     */
    public LocalLoggingContext setApp(final @Nullable String app) {
        this.app = app;
        return this;
    }

    @Override
    public @Nullable String getUser() {
        return user;
    }

    /**
     * Sets the user.
     * @param user the user name, or null
     * @return this context
     */
    public LocalLoggingContext setUser(final @Nullable String user) {
        this.user = user;
        return this;
    }

    /**
     * @return the entity that is being processed, or null
     */
    public @Nullable String getEntity() {
        return entity;
    }

    /**
     * Sets the entity.
     * @param entity the entity that is being processed, or null
     * @return this context
     */
    public LocalLoggingContext setEntity(final @Nullable String entity) {
        this.entity = entity;
        return this;
    }

}
