package com.eischet.janitor.versioning;

import org.jetbrains.annotations.Nullable;

/** A range of versions, where either end may be open. */
public class VersionRange {

    @Nullable
    private final Version min;

    @Nullable
    private final Version max;

    protected VersionRange(final @Nullable Version min, final @Nullable Version max) {
        this.min = min;
        this.max = max;
    }

    /**
     * @return the first version of the range, or null if the range has no lower limit
     */
    public @Nullable Version getMin() {
        return min;
    }

    /**
     * @return the last version of the range, or null if the range has no upper limit
     */
    public @Nullable Version getMax() {
        return max;
    }

    /**
     * @param version the first version of the range
     * @return a range with no upper limit
     */
    public static VersionRange startingWith(final Version version) {
        return new VersionRange(version, null);
    }

    /**
     * @param version the last version of the range
     * @return a range with no lower limit
     */
    public static VersionRange endingWith(final Version version) {
        return new VersionRange(null, version);
    }

    /**
     * @param version the first version that is not part of the range
     * @return a range with no lower limit
     */
    public static VersionRange endingBefore(final Version version) {
        return new VersionRange(null, new Version(version.getVersion() - 1));
    }

    /**
     * @param min the first version of the range
     * @param max the last version of the range
     * @return a range with both limits
     */
    public static VersionRange between(final Version min, final Version max) {
        return new VersionRange(min, max);
    }

    /**
     * @return a range that includes every version
     */
    public static VersionRange any() {
        return new VersionRange(null, null);
    }

    /**
     * @param version the version
     * @return true if the version lies within this range
     */
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
