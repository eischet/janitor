package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.*;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

public class JStringClass {


    public static final int INDEX_NOT_FOUND = -1;

    public static JString __format(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JString.of(self.janitorGetHostValue().formatted(arguments.getList().stream().map(JanitorObject::janitorGetHostValue).toArray()));
    }

    public static JString __expand(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return runningScript.expandTemplate(self, arguments);
    }

    public static JBinary __toBinaryUtf8(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return new JBinary(self.janitorGetHostValue().getBytes(StandardCharsets.UTF_8));
    }


    public static JString __get(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if (arguments.size() == 1) {
            int len = string.length();
            int start = arguments.get(0) != JNull.NULL ? JList.toIndex(arguments.getInt(0).getAsInt(), len) : 0;
            return JString.of(string.substring(start, start + 1));
        }
        if (arguments.size() == 2) {
            int len = string.length();
            final JanitorObject first = arguments.get(0);
            final JanitorObject second = arguments.get(1);
            // System.err.println("SUBSTRING " + first + " : " + second);
            if (first == JNull.NULL && second == JNull.NULL) {
                // System.err.println("case 1");
                return JString.of(string);
            } else if (first == JNull.NULL) {
                // System.err.println("case 2");
                int end = arguments.getInt(1).getAsInt();
                if (end < 0) {
                    // System.err.println("case 2 inverted end: " + end + " => " + (len + end));
                    end = len + end;
                }
                final String str = string.substring(0, end);
                return JString.of(str);
            } else if (second == JNull.NULL) {
                // System.err.println("case 3");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = string.length();
                return JString.of(string.substring(start, end));
            } else {
                // System.err.println("case 4");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = JList.toIndex(arguments.getInt(1).getAsInt(), len);
                final String str = string.substring(Math.min(start, end), Math.max(start, end));
                if (end < start) {
                    return JString.of(new StringBuilder(str).reverse().toString());
                }
                return JString.of(str);
            }
        }
        /*
        LATER: auch hier ein stepping unterstützen
        if (arguments.size() == 3) {

        }
         */
        throw new JanitorArgumentException(runningScript, "invalid arguments: " + arguments);
    }

    public static JFloat __toFloat(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if ("".equals(string) || string.isBlank()) {
            return JFloat.of(0);
        }
        try {
            final double iv = Double.parseDouble(string);
            return JFloat.of(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(runningScript, "invalid value for toFloat conversion: '" + string + "': " + e.getMessage());
        }
    }


    public static JInt __toInt(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if ("".equals(string) || string.isBlank()) {
            return JInt.of(0);
        }
        try {
            final long iv = Long.parseLong(string, 10);
            return JInt.of(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(runningScript, "invalid value for toInt conversion: '" + string + "': " + e.getMessage());
        }
    }

    public static JString __toUpperCase(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(self.janitorGetHostValue().toUpperCase(Locale.ROOT));
    }

    public static JString __toLowerCase(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(self.janitorGetHostValue().toLowerCase(Locale.ROOT));
    }

    public static JInt __count(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JInt.of(countMatches(self.janitorGetHostValue(), arguments.require(1).getString(0).janitorGetHostValue()));
    }

    public static JString __replace(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replace(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return JString.of(result);
    }

    public static JString __replaceFirst(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceFirst(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return JString.of(result);
    }

    public static JString __replaceAll(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceAll(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return JString.of(result);
    }

    public static JString __trim(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(self.janitorGetHostValue().trim());
    }

    public static JanitorObject __length(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JInt.of(self.janitorGetHostValue().length());
    }

    public static JanitorObject __empty(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JBool.map(self.janitorGetHostValue().isEmpty());
    }

    public static JBool __contains(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.map(self.janitorGetHostValue().contains(arguments.getString(0).janitorGetHostValue()));
    }

    public static JBool __containsIgnoreCase(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.map(self.janitorGetHostValue().toLowerCase(Locale.GERMANY)
            .contains(arguments.getString(0).janitorGetHostValue().toLowerCase(Locale.GERMANY)));
    }


    public static JList __splitLines(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final ArrayList<JanitorObject> list = new ArrayList<>();
        for (final String s : self.janitorGetHostValue().split("\r?\n\r?")) {
            list.add(JString.ofNullable(s));
        }
        return new JList(list);
    }

    public static JBool __endsWith(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            final String string = self.janitorGetHostValue();
            return JBool.map(string != null && string.endsWith(with.janitorToString()));
        }
    }

    private static final Pattern NUMBERS_ONLY = Pattern.compile("^\\d+$");
    private static final Pattern NUMBERS_AT_THE_START = Pattern.compile("^\\d+.*");

    public static JBool __startsWithNumbers(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return JBool.map(string != null && NUMBERS_AT_THE_START.matcher(string).matches());
    }

    public static JBool __isNumeric(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return JBool.map(string != null && NUMBERS_ONLY.matcher(string).matches());
    }

    public static JBool __startsWith(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            final String string = self.janitorGetHostValue();
            return JBool.map(string != null && string.startsWith(with.janitorGetHostValue()));
        }
    }

    public static JInt __indexOf(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JInt.of(self.janitorGetHostValue().indexOf(arguments.getString(0).janitorGetHostValue()));
    }

    public static JString __substring(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final int from = (int) arguments.getInt(0).getValue();
        final int to = arguments.size() > 1 ? (int) arguments.getInt(1).getValue() : self.janitorGetHostValue().length();
        return JString.of(self.janitorGetHostValue().substring(from, to));
    }

    public static JString __removeLeadingZeros(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        var s = self.janitorGetHostValue();
        while (s.startsWith("0")) {
            s = s.substring(1);
        }
        return JString.of(s);
    }


    public static JanitorObject __parseDate(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return JDate.parse(runningScript, self.janitorGetHostValue(), format);
    }

    public static JanitorObject __parseDateTime(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return JDateTime.parse(runningScript, self.janitorGetHostValue(), format);
    }

    public static JanitorObject __cutFilename(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        return JString.of(cutFilename(self.janitorGetHostValue(), arguments.getInt(0).getAsInt()));
    }

    public static @Nullable String cutFilename(@Nullable String filename, int maxLength) {
        if (filename == null) {
            return null;
        }
        File file = new File(filename.trim());
        filename = file.getName();
        if (filename.length() <= maxLength) {
            return filename;
        }
        String[] parts = filename.split("\\.(?=[^\\.]+$)");
        if (parts.length > 1) {
            filename = parts[0].substring(0, maxLength - parts[1].length() - 1).trim() + "." + parts[1].trim();
        } else {
            filename = parts[0].substring(0, maxLength).trim();
        }
        return filename;
    }


    public static JanitorObject __urlEncode(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(URLEncoder.encode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    public static JanitorObject __urlDecode(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(URLDecoder.decode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    public static JanitorObject __decodeBase64(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return new JBinary(Base64.getDecoder().decode(self.janitorGetHostValue()));
    }


    private static boolean isEmpty(CharSequence str) {
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
}