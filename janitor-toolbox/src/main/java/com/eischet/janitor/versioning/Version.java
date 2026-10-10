// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.versioning;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A version number of the form {@code major.minor.patch}, where minor and patch are below 1000.
 * Versions are stored as a single number, so they can be compared quickly.
 */
public class Version implements Comparable<Version> {

    /** Thrown when a version string cannot be parsed. */
    public static class MalformedVersion extends Exception {
        public MalformedVersion(final String message) {
            super(message);
        }

        public MalformedVersion(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    private final long version;
    private final String versionString;

    public Version(final String versionString) throws MalformedVersion {
        if (versionString == null || versionString.isEmpty()) {
            throw new MalformedVersion("Invalid Version: null or empty. Expected Format: number.number.number, with the last two parts not exceeding 999.");
        }
        final String[] parts = versionString.split("\\.");
        if (parts.length != 3) {
            throw new MalformedVersion("Invalid Version '" + versionString + "'. Expected Format: number.number.number, with the last two parts not exceeding 999.");
        }
        try {
            this.version = (Long.parseLong(parts[0], 10) * 1000000L) + below1000(Long.parseLong(parts[1], 10)) * 1000L + below1000(Integer.parseInt(parts[2], 10));
            this.versionString = versionString;
        } catch (MalformedVersion error) {
            throw new MalformedVersion("Invalid Version '" + versionString + "'. Expected Format: number.number.number, with the last two parts not exceeding 999.", error);
        } catch (NumberFormatException error) {
            throw new MalformedVersion("Invalid Version '" + versionString + "'. Expected Format: number.number.number, with all parts as numbers.", error);
        }
    }

    private static long below1000(final long i) throws MalformedVersion {
        if (i >= 1000) {
            throw new MalformedVersion("part " + i + " exceeds three digits");
        }
        return i;
    }

    /**
     * Parses a version string.
     * @param versionString the string, in the form {@code major.minor.patch}
     * @return the version
     * @throws RuntimeException if the string is malformed
     */
    public static Version ofWithRuntimeError(final String versionString) {
        try {
            return new Version(versionString);
        } catch (MalformedVersion error) {
            throw new RuntimeException("Failed to parse version string: " + versionString, error);
        }
    }

    public Version(final long version) {
        this.version = version;
        long a = version / 1000000 % 1000;
        long b = version / 1000 % 1000;
        long c = version % 1000;
        this.versionString = String.format("%s.%s.%s", a, b, c);
    }

    /**
     * @return the series of this version, i.e. its major and minor number, e.g. "1.2.x"
     */
    public String getSeries() {
        long a = version / 1000000 % 1000;
        long b = version / 1000 % 1000;
        return String.format("%s.%s.x", a, b);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final Version version1)) return false;
        return version == version1.version;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(version);
    }

    /**
     * @param version the version, may be null
     * @param defaultVersion the version to use if the first one is null
     * @return the version, or the default version if it was null
     */
    public static @NotNull Version coalesce(final @Nullable Version version, final @NotNull Version defaultVersion) {
        return version == null ? defaultVersion : version;
    }

    /**
     * @param version the version, may be null
     * @return the version, or the earliest possible version if it was null
     */
    public static @NotNull Version coalesceMin(final @Nullable Version version) {
        return coalesce(version, EARLIEST);
    }

    /**
     * @param version the version, may be null
     * @return the version, or the latest possible version if it was null
     */
    public static @NotNull Version coalesceMax(final @Nullable Version version) {
        return coalesce(version, LATEST);
    }

    /**
     * @return the numeric representation of this version
     */
    public long getVersion() {
        return version;
    }

    /**
     * @return the version as a string
     */
    public String getVersionString() {
        return versionString;
    }

    @Override
    public String toString() {
        return versionString;
    }

    /**
     * @param number the numeric representation of the version
     * @return the version
     */
    public static Version of(final long number) {
        return new Version(number);
    }

    public static final Version EARLIEST = Version.of(0);
    public static final Version LATEST = Version.of(Long.MAX_VALUE);

    @Override
    public int compareTo(@NotNull final Version o) {
        return Long.compare(version, o.version);
    }

}
