package com.eischet.janitor.logging.jul;

public class LocalLoggingContext implements ILoggingContext {
    private String app;
    private String user;

    public String getApp() {
        return app;
    }

    public LocalLoggingContext setApp(final String app) {
        this.app = app;
        return this;
    }

    @Override
    public String getUser() {
        return user;
    }

    public LocalLoggingContext setUser(final String user) {
        this.user = user;
        return this;
    }

}
