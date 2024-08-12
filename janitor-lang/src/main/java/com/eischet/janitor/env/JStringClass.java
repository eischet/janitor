package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.toolbox.strings.StringHelpers;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Operations for string objects.
 * TODO: wrap these with a DispatchTable, to allow greater customisation options to hosts.
 */
public class JStringClass {


    public static JString __format(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getEnvironment().getBuiltins().string(self.janitorGetHostValue().formatted(arguments.getList().stream().map(JanitorObject::janitorGetHostValue).toArray()));
    }

    public static JString __expand(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.expandTemplate(self, arguments);
    }

    public static JBinary __toBinaryUtf8(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getEnvironment().getBuiltins().binary(self.janitorGetHostValue().getBytes(StandardCharsets.UTF_8));
    }


    public static JString __get(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if (arguments.size() == 1) {
            int len = string.length();
            int start = arguments.get(0) != JNull.NULL ? JList.toIndex(arguments.getInt(0).getAsInt(), len) : 0;
            return process.getEnvironment().getBuiltins().string(string.substring(start, start + 1));
        }
        if (arguments.size() == 2) {
            int len = string.length();
            final JanitorObject first = arguments.get(0);
            final JanitorObject second = arguments.get(1);
            // System.err.println("SUBSTRING " + first + " : " + second);
            if (first == JNull.NULL && second == JNull.NULL) {
                // System.err.println("case 1");
                return process.getEnvironment().getBuiltins().string(string);
            } else if (first == JNull.NULL) {
                // System.err.println("case 2");
                int end = arguments.getInt(1).getAsInt();
                if (end < 0) {
                    // System.err.println("case 2 inverted end: " + end + " => " + (len + end));
                    end = len + end;
                }
                final String str = string.substring(0, end);
                return process.getEnvironment().getBuiltins().string(str);
            } else if (second == JNull.NULL) {
                // System.err.println("case 3");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = string.length();
                return process.getEnvironment().getBuiltins().string(string.substring(start, end));
            } else {
                // System.err.println("case 4");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = JList.toIndex(arguments.getInt(1).getAsInt(), len);
                final String str = string.substring(Math.min(start, end), Math.max(start, end));
                if (end < start) {
                    return process.getEnvironment().getBuiltins().string(new StringBuilder(str).reverse().toString());
                }
                return process.getEnvironment().getBuiltins().string(str);
            }
        }
        /*
        LATER: auch hier ein stepping unterstützen
        if (arguments.size() == 3) {

        }
         */
        throw new JanitorArgumentException(process, "invalid arguments: " + arguments);
    }

    public static JFloat __toFloat(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if ("".equals(string) || string.isBlank()) {
            return process.getEnvironment().getBuiltins().floatingPoint(0);
        }
        try {
            final double iv = Double.parseDouble(string);
            return process.getEnvironment().getBuiltins().floatingPoint(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(process, "invalid value for toFloat conversion: '" + string + "': " + e.getMessage());
        }
    }


    public static JInt __toInt(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if ("".equals(string) || string.isBlank()) {
            return process.getEnvironment().getBuiltins().integer(0);
        }
        try {
            final long iv = Long.parseLong(string, 10);
            return process.getEnvironment().getBuiltins().integer(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(process, "invalid value for toInt conversion: '" + string + "': " + e.getMessage());
        }
    }

    public static JString __toUpperCase(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(self.janitorGetHostValue().toUpperCase(Locale.ROOT));
    }

    public static JString __toLowerCase(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltins().string(self.janitorGetHostValue().toLowerCase(Locale.ROOT));
    }

    public static JInt __count(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getEnvironment().getBuiltins().integer(StringHelpers.countMatches(self.janitorGetHostValue(), arguments.require(1).getString(0).janitorGetHostValue()));
    }

    public static JString __replace(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replace(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return process.getEnvironment().getBuiltins().string(result);
    }

    public static JString __replaceFirst(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceFirst(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return process.getEnvironment().getBuiltins().string(result);
    }

    public static JString __replaceAll(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceAll(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return process.getEnvironment().getBuiltins().string(result);
    }

    public static JString __trim(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltins().string(self.janitorGetHostValue().trim());
    }

    public static JanitorObject __length(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltins().integer(self.janitorGetHostValue().length());
    }

    public static JanitorObject __empty(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JBool.of(self.janitorGetHostValue().isEmpty());
    }

    public static JBool __contains(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.of(self.janitorGetHostValue().contains(arguments.getString(0).janitorGetHostValue()));
    }

    public static JBool __containsIgnoreCase(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.of(self.janitorGetHostValue().toLowerCase(Locale.GERMANY)
            .contains(arguments.getString(0).janitorGetHostValue().toLowerCase(Locale.GERMANY)));
    }


    public static JList __splitLines(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final ArrayList<JanitorObject> list = new ArrayList<>();
        for (final String s : self.janitorGetHostValue().split("\r?\n\r?")) {
            list.add(process.getEnvironment().getBuiltins().nullableString(s));
        }
        return process.getEnvironment().getBuiltins().list(list);
    }

    public static JBool __endsWith(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            final String string = self.janitorGetHostValue();
            return JBool.of(string != null && string.endsWith(with.janitorToString()));
        }
    }

    private static final Pattern NUMBERS_ONLY = Pattern.compile("^\\d+$");
    private static final Pattern NUMBERS_AT_THE_START = Pattern.compile("^\\d+.*");

    public static JBool __startsWithNumbers(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return JBool.of(string != null && NUMBERS_AT_THE_START.matcher(string).matches());
    }

    public static JBool __isNumeric(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return JBool.of(string != null && NUMBERS_ONLY.matcher(string).matches());
    }

    public static JBool __startsWith(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            final String string = self.janitorGetHostValue();
            return JBool.of(string != null && string.startsWith(with.janitorGetHostValue()));
        }
    }

    public static JInt __indexOf(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getEnvironment().getBuiltins().integer(self.janitorGetHostValue().indexOf(arguments.getString(0).janitorGetHostValue()));
    }

    public static JString __substring(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final int from = (int) arguments.getInt(0).getValue();
        final int to = arguments.size() > 1 ? (int) arguments.getInt(1).getValue() : self.janitorGetHostValue().length();
        return process.getEnvironment().getBuiltins().string(self.janitorGetHostValue().substring(from, to));
    }

    public static JString __removeLeadingZeros(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        var s = self.janitorGetHostValue();
        while (s.startsWith("0")) {
            s = s.substring(1);
        }
        return process.getEnvironment().getBuiltins().string(s);
    }


    public static JanitorObject __parseDate(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return  process.getBuiltins().parseNullableDate(process, self.janitorGetHostValue(), format);
    }

    public static JanitorObject __parseDateTime(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return JDateTime.parse(process, self.janitorGetHostValue(), format);
    }

    public static JanitorObject __cutFilename(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        // TODO: remove this from the language, as it is only of interest for a single application!
        arguments.require(1);
        return process.getEnvironment().getBuiltins().string(cutFilename(self.janitorGetHostValue(), arguments.getInt(0).getAsInt()));
    }

    public static @Nullable String cutFilename(@Nullable String filename, int maxLength) {
        // TODO: remove this from the language, as it is only of interest for a single application!
        if (filename == null) {
            return null;
        }
        File file = new File(filename.trim());
        filename = file.getName();
        if (filename.length() <= maxLength) {
            return filename;
        }
        String[] parts = filename.split("\\.(?=[^.]+$)");
        if (parts.length > 1) {
            filename = parts[0].substring(0, maxLength - parts[1].length() - 1).trim() + "." + parts[1].trim();
        } else {
            filename = parts[0].substring(0, maxLength).trim();
        }
        return filename;
    }


    public static JanitorObject __urlEncode(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltins().string(URLEncoder.encode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    public static JanitorObject __urlDecode(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltins().string(URLDecoder.decode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    public static JanitorObject __decodeBase64(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getEnvironment().getBuiltins().binary(Base64.getDecoder().decode(self.janitorGetHostValue()));
    }


}
