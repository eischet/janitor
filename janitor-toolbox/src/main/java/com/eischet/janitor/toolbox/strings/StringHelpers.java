package com.eischet.janitor.toolbox.strings;

import java.util.Collection;
import java.util.stream.IntStream;

public class StringHelpers {

    public static final int DEFAULT_CUT = 120;
    public static final int INDEX_NOT_FOUND = -1;
    // printHexBinary: lifted from jakarta.xml.bind.DatatypeConverterImpl (jakarta)
    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String cut(String s, int size) {
        if (s == null) {
            return "";
        }
        if (s.isEmpty() || s.length() <= size) {
            return s;
        }
        return s.substring(0, size) + "[...]";
    }

    public static String cut(String s) {
        return cut(s, DEFAULT_CUT);
    }

    public static String compressed(String s, int size) {
        return cut(s.replaceAll("\\s+", " "), size);
    }

    public static String join(String sep, Collection<String> collection) {
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

    public static String disarm(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;");
    }

    public static String removeLastCharacter(final String s) {
        return (s == null || s.length() < 1) ? "" : s.substring(0, s.length() - 1);
    }

    public static boolean nullOrEmpty(final String s) {
        return s == null || s.isEmpty();
    }

    public static boolean notEmpty(final String s) {
        return !nullOrEmpty(s);
    }

    public static boolean containText(final String text, final String... candidates) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        final String lowText = text.toLowerCase();
        for (final String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase().contains(lowText)) {
                return true;
            }
        }
        return false;
    }

    public static String repeat(final String s, final long num) {
        StringBuilder repetitions = new StringBuilder();
        for (long i = 0; i < num; i++) {
            repetitions.append(s);
        }
        return repetitions.toString();
    }

    public static Long asLongInstance(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s, 10);
        } catch (Exception e) {
            return null;
        }
    }

    public static int asInt(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s, 10);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String toUpper(final String s) {
        return s == null ? s : s.toUpperCase();
    }

    public static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotNullOrEmpty(final String string) {
        return !isNullOrEmpty(string);
    }

    public static String cutNullable(String s, int size) {
        return s == null ? null : cut(s, size);
    }

    public static String toValidFilename(String filename) {
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

    public static String coalesce(final String str) {
        return str == null ? "" : str;
    }

    public static String coalesceNullOrBlank(final String str, final String fallback) {
        return str == null || str.isBlank() ? fallback : str;
    }

    public static int compareNullable(final String s1, final String s2) {
        return coalesce(s1).compareTo(coalesce(s2));
    }

    public static String formatBytes(final long size) {
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

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
    }

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

    public static boolean in(String needle, String... haystack) {
        return IntStream.range(0, haystack.length).anyMatch(i -> haystack[i].equals(needle));
    }

    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}
