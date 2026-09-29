package com.eischet.janitor.versioning;

import org.jetbrains.annotations.Nullable;

public class VersionRange {

    @Nullable
    private final Version min;

    @Nullable
    private final Version max;

    protected VersionRange(final @Nullable Version min, final @Nullable Version max) {
        this.min = min;
        this.max = max;
    }

    public @Nullable Version getMin() {
        return min;
    }

    public @Nullable Version getMax() {
        return max;
    }

    public static VersionRange startingWith(final Version version) {
        return new VersionRange(version, null);
    }

    public static VersionRange endingWith(final Version version) {
        return new VersionRange(null, version);
    }

    public static VersionRange endingBefore(final Version version) {
        return new VersionRange(null, new Version(version.getVersion() - 1));
    }

    public static VersionRange between(final Version min, final Version max) {
        return new VersionRange(min, max);
    }

    public static VersionRange any() {
        return new VersionRange(null, null);
    }

    public boolean includes(final Version version) {
        final boolean minOk = min == null || min.getVersion() <= version.getVersion();
        final boolean maxOk = max == null || max.getVersion() >= version.getVersion();
        return minOk && maxOk;
    }

    @Override
    public String toString() {
        return (min == null ? "any" : min.toString()) + ".." + (max == null ? "any" : max.toString());
    }
}
