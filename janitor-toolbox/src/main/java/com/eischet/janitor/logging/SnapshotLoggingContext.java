package com.eischet.janitor.logging;

public class SnapshotLoggingContext implements ILoggingContext {
    private final String app;
    private final String user;
    private final String entity;

    public SnapshotLoggingContext(final LocalLoggingContext localLoggingContext, final boolean forError) {
        this.app = localLoggingContext.getApp();
        this.user = localLoggingContext.getUser();
        this.entity = localLoggingContext.getEntity();
    }

    @Override
    public String getApp() {
        return app;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getEntity() {
        return entity;
    }
}
