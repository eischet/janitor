package com.eischet.janitor.logging.jul;

public class SnapshotLoggingContext implements ILoggingContext {
    private final String app;
    private final String user;

    public SnapshotLoggingContext(final LocalLoggingContext localLoggingContext, final boolean forError) {
        this.app = localLoggingContext.getApp();
        this.user = localLoggingContext.getUser();
    }

    @Override
    public String getApp() {
        return app;
    }

    @Override
    public String getUser() {
        return user;
    }

}
