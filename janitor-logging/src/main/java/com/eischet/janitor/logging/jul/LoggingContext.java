package com.eischet.janitor.logging.jul;

public class LoggingContext {

    public static final ThreadLocal<LocalLoggingContext> localContext = ThreadLocal.withInitial(LocalLoggingContext::new);

    @SuppressWarnings("UnusedReturnValue")
    public static LocalLoggingContext setApp(final String app) {
        return localContext.get().setApp(app);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static LocalLoggingContext setUser(final String user) {
        return localContext.get().setUser(user);
    }

    public static void clear() {
        localContext.remove();
    }

    public static LocalLoggingContext setEntity(final String entity) { return localContext.get().setEntity(entity);}

    protected static ILoggingContext getSnapshot(final boolean forError) {
        return new SnapshotLoggingContext(localContext.get(), forError);
    }

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
