// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;

import org.jetbrains.annotations.Nullable;

/**
 * Sets app, user and entity of the logging context of the current thread for a stretch of work and puts back what was there
 * before when it is closed, so that the log names who and what a message was written for.
 * <p>
 * Unlike {@link LoggingContext#clear()}, closing a scope leaves the context of an enclosing piece of work intact, e.g. a request
 * around the job it starts. A scope must be closed in the thread that opened it.
 * </p>
 */
public final class LoggingScope implements AutoCloseable {

    private final @Nullable String previousApp;
    private final @Nullable String previousUser;
    private final @Nullable String previousEntity;

    private LoggingScope(final @Nullable String app, final @Nullable String user, final @Nullable String entity, final boolean keepWhereNull) {
        final LocalLoggingContext context = LoggingContext.localContext.get();
        previousApp = context.getApp();
        previousUser = context.getUser();
        previousEntity = context.getEntity();
        if (app != null || !keepWhereNull) {
            context.setApp(app);
        }
        if (user != null || !keepWhereNull) {
            context.setUser(user);
        }
        if (entity != null || !keepWhereNull) {
            context.setEntity(entity);
        }
    }

    /**
     * Opens a scope on the current thread; a value given as null keeps the one already set.
     *
     * @param app    the app or kind of work; null keeps the current one
     * @param user   the user the work is done for; null keeps the current one
     * @param entity what is being worked on; null keeps the current one
     * @return the scope, to be closed in this thread
     */
    public static LoggingScope open(final @Nullable String app, final @Nullable String user, final @Nullable String entity) {
        return new LoggingScope(app, user, entity, true);
    }

    /**
     * Opens a scope that sets all three values as given, null included, e.g. for a task that carries the context of whoever
     * submitted it to another thread.
     *
     * @param app    the app or kind of work
     * @param user   the user the work is done for
     * @param entity what is being worked on
     * @return the scope, to be closed in this thread
     */
    public static LoggingScope replacing(final @Nullable String app, final @Nullable String user, final @Nullable String entity) {
        return new LoggingScope(app, user, entity, false);
    }

    /**
     * Opens a scope that sets the app only if the thread has none yet.
     *
     * @param defaultApp the app to set if there is none
     * @param user       the user the work is done for; null keeps the current one
     * @param entity     what is being worked on; null keeps the current one
     * @return the scope, to be closed in this thread
     */
    public static LoggingScope openKeepingApp(final String defaultApp, final @Nullable String user, final @Nullable String entity) {
        return new LoggingScope(LoggingContext.localContext.get().getApp() == null ? defaultApp : null, user, entity, true);
    }

    /**
     * Sets the user within this scope, e.g. once a request has been authenticated; closing the scope puts back the previous one.
     *
     * @param user the user name
     */
    public void setUser(final @Nullable String user) {
        LoggingContext.setUser(user);
    }

    /**
     * Sets the entity within this scope; closing the scope puts back the previous one.
     *
     * @param entity what is being worked on
     */
    public void setEntity(final @Nullable String entity) {
        LoggingContext.setEntity(entity);
    }

    @Override
    public void close() {
        // fetched anew: the work may have cleared the thread's context in between
        final LocalLoggingContext context = LoggingContext.localContext.get();
        context.setApp(previousApp);
        context.setUser(previousUser);
        context.setEntity(previousEntity);
    }
}
