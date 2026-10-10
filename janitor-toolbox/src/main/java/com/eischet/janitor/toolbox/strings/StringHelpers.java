// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.strings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.stream.IntStream;

/** Static helper functions for working with strings. */
public class StringHelpers {

    public static final int DEFAULT_CUT = 120;
    public static final int INDEX_NOT_FOUND = -1;
    // printHexBinary: lifted from jakarta.xml.bind.DatatypeConverterImpl (jakarta)
    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    /**
     * Shortens a string, marking the cut with "[...]".
     * @param s the string, may be null
     * @param size the maximum length before the string is cut
     * @return the string, or an empty string if it was null
     */
    public static String cut(String s, int size) {
        if (s == null) {
            return "";
        }
        if (s.isEmpty() || s.length() <= size) {
            return s;
        }
        return s.substring(0, size) + "[...]";
    }

    /**
     * Shortens a string to {@link #DEFAULT_CUT} characters, marking the cut with "[...]".
     * @param s the string, may be null
     * @return the string, or an empty string if it was null
     */
    public static String cut(String s) {
        return cut(s, DEFAULT_CUT);
    }

    /**
     * Collapses all whitespace to single spaces, then shortens the string.
     * @param s the string
     * @param size the maximum length before the string is cut
     * @return the compressed string
     */
    public static String compressed(String s, int size) {
        return cut(s.replaceAll("\\s+", " "), size);
    }

    /**
     * Joins strings with a separator.
     * @param sep the separator
     * @param collection the strings
     * @return the joined string
     */
    public static @NotNull String join(@NotNull String sep, @NotNull @Unmodifiable Collection<String> collection) {
        StringBuilder out = new StringBuilder();
        int count = 0;
        for (String element : collection) {
            ++count;
            if (count > 1) {
                out.append(sep);
            }
            out.append(element);
        }
        return out.toString();
    }

    /**
     * Escapes the string for use in HTML.
     * @param s the string, may be null
     * @return the escaped string, or an empty string if it was null
     */
    public static @NotNull String disarm(@Nullable String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;");
    }

    /**
     * Escapes the string for use in HTML, and turns line breaks into {@code <br/>}.
     * @param s the string, may be null
     * @return the escaped string
     */
    public static String disarmAndPreformat(final @Nullable String s) {
        return disarm(s).replace("\n", "<br/>");
    }

    /**
     * @param s the string, may be null
     * @return the string without its last character, or an empty string if it was null or empty
     */
    public static String removeLastCharacter(final String s) {
        return (s == null || s.length() < 1) ? "" : s.substring(0, s.length() - 1);
    }

    /**
     * @param s the string
     * @return true if the string is null or empty
     */
    public static boolean nullOrEmpty(@Nullable final String s) {
        return s == null || s.isEmpty();
    }

    /**
     * @param s the string
     * @return true if the string is neither null nor empty
     */
    public static boolean notEmpty(@Nullable final String s) {
        return !nullOrEmpty(s);
    }


    /**
     * Repeats a string.
     * @param s the string
     * @param num how often to repeat it
     * @return the repeated string
     */
    public static @NotNull String repeat(final @NotNull String s, final long num) {
        StringBuilder repetitions = new StringBuilder();
        for (long i = 0; i < num; i++) {
            repetitions.append(s);
        }
        return repetitions.toString();
    }

    /**
     * Parses a decimal long, leniently.
     * @param s the string, may be null
     * @return the number, or null if the string is null, empty or not a number
     */
    public static @Nullable Long asLongInstance(final @Nullable String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s, 10);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a decimal integer, leniently.
     * @param s the string
     * @return the number, or 0 if the string is null, empty or not a number
     */
    public static int asInt(final @Nullable String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s, 10);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * @param s the string, may be null
     * @return the string in upper case, or null if it was null
     */
    public static @Nullable String toUpper(final @Nullable String s) {
        return s == null ? s : s.toUpperCase();
    }

    /**
     * @param string the string
     * @return true if the string is null or empty
     */
    public static boolean isNullOrEmpty(final @Nullable String string) {
        return string == null || string.isEmpty();
    }

    /**
     * @param string the string
     * @return true if the string is neither null nor empty
     */
    public static boolean isNotNullOrEmpty(final @Nullable String string) {
        return !isNullOrEmpty(string);
    }

    /**
     * Shortens a string, marking the cut with "[...]".
     * @param s the string, may be null
     * @param size the maximum length before the string is cut
     * @return the string, or null if it was null
     */
    public static String cutNullable(@Nullable String s, int size) {
        return s == null ? null : cut(s, size);
    }

    /**
     * Removes characters that are not allowed in file names.
     * @param filename the file name, may be null
     * @return the cleaned file name
     */
    public static @Nullable String toValidFilename(@Nullable String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        filename = filename.replace("..", ".");
        filename = filename.replace("#", "");
        filename = filename.replace(":", "");
        filename = filename.replace("\\", "");
        filename = filename.replace("/", "");
        filename = filename.replace("=", "");
        filename = filename.replace("&", "");
        filename = filename.replace("%", "");
        filename = filename.replace(",", "");
        filename = filename.replace("--", "-");
        filename = filename.replace(";", "");
        if (filename.startsWith(".")) {
            filename = "attachment" + filename;
        }
        return filename;
    }

    /**
     * @param str the string, may be null
     * @return the string, or an empty string if it was null
     */
    public static @NotNull String coalesce(final @Nullable String str) {
        return str == null ? "" : str;
    }

    /**
     * @param str the string, may be null
     * @param fallback the value to use if the string is null or blank
     * @return the string, or the fallback if it was null or blank
     */
    public static @NotNull String coalesceNullOrBlank(final String str, final String fallback) {
        return str == null || str.isBlank() ? fallback : str;
    }

    /**
     * Compares two strings, treating null as an empty string.
     * @param s1 the first string
     * @param s2 the second string
     * @return the result of comparing the strings
     */
    public static int compareNullable(final String s1, final String s2) {
        return coalesce(s1).compareTo(coalesce(s2));
    }

    /**
     * Formats a size in bytes in a human-readable way, as bytes, KB or MB.
     * @param size the size in bytes
     * @return the formatted size
     */
    public static @NotNull String formatBytes(final long size) {
        if (size < 1024) {
            return size + " bytes";
        }
        if (size < 1024 * 1024) {
            return String.format("%.02f KB", ((double) size) / 1024.0);
        }
        return String.format("%.02f MB", ((double) size) / 1024.0 / 1024.0);
    }

    static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        if (cs instanceof String) {
            return ((String) cs).indexOf(searchChar.toString(), start);
        } else if (cs instanceof StringBuilder) {
            return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
        } else if (cs instanceof StringBuffer) {
            return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
        }
        return cs.toString().indexOf(searchChar.toString(), start);
    }

    /**
     * @param str the character sequence
     * @return true if the sequence is null or empty
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
    }

    /**
     * Counts how often a text occurs in another, without overlaps.
     * @param str the text to search in
     * @param sub the text to look for
     * @return the number of occurrences
     */
    public static int countMatches(final CharSequence str, final CharSequence sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = indexOf(str, sub, idx)) != INDEX_NOT_FOUND) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * @param needle the string to look for
     * @param haystack the strings to look in
     * @return true if the needle is one of the strings in the haystack
     */
    public static boolean in(String needle, String... haystack) {
        return IntStream.range(0, haystack.length).anyMatch(i -> haystack[i].equals(needle));
    }

    /**
     * Converts bytes to a string of hexadecimal digits.
     * @param data the bytes
     * @return the hexadecimal string, using upper case
     */
    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}
