package com.eischet.janitor.orm.entity;

public class ChangeTrackerNop implements ChangeTracker {

    public static ChangeTrackerNop INSTANCE = new ChangeTrackerNop();

    private ChangeTrackerNop() {
    }
}
