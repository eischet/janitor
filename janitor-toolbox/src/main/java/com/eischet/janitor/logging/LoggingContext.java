// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;


/**
 * Thread-local logging context, which holds the application, user and entity that the current thread is working for,
 * so that log messages can be enriched with that information.
 */
public class LoggingContext {

    public static final ThreadLocal<LocalLoggingContext> localContext = ThreadLocal.withInitial(LocalLoggingContext::new);

    /**
     * Sets the application for the current thread.
     * @param app the application name
     * @return the logging context of the current thread
     */
    @SuppressWarnings("UnusedReturnValue")
    public static LocalLoggingContext setApp(final String app) {
        return localContext.get().setApp(app);
    }

    /**
     * Sets the user for the current thread.
     * @param user the user name
     * @return the logging context of the current thread
     */
    @SuppressWarnings("UnusedReturnValue")
    public static LocalLoggingContext setUser(final String user) {
        return localContext.get().setUser(user);
    }

    /** Clears the logging context of the current thread. */
    public static void clear() {
        localContext.remove();
    }

    /**
     * Sets the entity for the current thread.
     * @param entity the entity that is being processed
     * @return the logging context of the current thread
     */
    public static LocalLoggingContext setEntity(final String entity) { return localContext.get().setEntity(entity);}

    /**
     * Takes an immutable snapshot of the logging context of the current thread.
     * @param forError true if the snapshot is meant for reporting an error
     * @return the snapshot
     */
    public static ILoggingContext getSnapshot(final boolean forError) {
        return new SnapshotLoggingContext(localContext.get(), forError);
    }

    /**
     * Runs some code with the given entity set as the current entity, and restores the previous entity afterwards.
     * @param entity the entity
     * @param runnable the code to run
     */
    public static void withEntity(final String entity, final Runnable runnable) {
        final LocalLoggingContext myContext = localContext.get();
        final String previousEntity = myContext.getEntity();
        myContext.setEntity(entity);
        try {
            runnable.run();
        } finally {
            myContext.setEntity(previousEntity);
        }
    }

}
