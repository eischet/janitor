package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.toolbox.strings.StringHelpers;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Operations for string objects.
 * TODO: wrap these with a DispatchTable, to allow greater customisation options to hosts.
 */
public class JStringClass {

    public static final String STRING_CLASS = """
            The String class represents sequences of characters.
    
            String instances themselves are immutable, i.e. they cannot be changed.
            But you can call methods that return a different String.
            
            """;

    public static final String STRING_LENGTH = "String.length(): Returns the number of characters in the string.";

    public static JanitorObject length(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().integer(self.janitorGetHostValue().length());
    }

    public static final String STRING_TRIM = "String.trim(): Returns the String with leading and trailing spaces removed";

    public static JString trim(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(self.janitorGetHostValue().trim());
    }

    public static JString format(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().string(self.janitorGetHostValue().formatted(arguments.getList().stream().map(JanitorObject::janitorGetHostValue).toArray()));
    }

    public static JString expand(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.expandTemplate(self, arguments);
    }

    public static JBinary toBinaryUtf8(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().binary(self.janitorGetHostValue().getBytes(StandardCharsets.UTF_8));
    }

    public static JBinary encode(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String enc = arguments.getOptionalStringValue(0, "UTF-8");
        try {
            return process.getBuiltins().binary(self.janitorGetHostValue().getBytes(enc));
        } catch (UnsupportedEncodingException e) {
            throw new JanitorNativeException(process, "invalid binary encoding: " + enc, e);
        }
    }


    public static JString indexedGet(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if (arguments.size() == 1) {
            int len = string.length();
            int start = arguments.get(0) != JNull.NULL ? JList.toIndex(arguments.getInt(0).getAsInt(), len) : 0;
            return process.getBuiltins().string(string.substring(start, start + 1));
        }
        if (arguments.size() == 2) {
            int len = string.length();
            final JanitorObject first = arguments.get(0);
            final JanitorObject second = arguments.get(1);
            // System.err.println("SUBSTRING " + first + " : " + second);
            if (first == JNull.NULL && second == JNull.NULL) {
                // System.err.println("case 1");
                return process.getBuiltins().string(string);
            } else if (first == JNull.NULL) {
                // System.err.println("case 2");
                int end = arguments.getInt(1).getAsInt();
                if (end < 0) {
                    // System.err.println("case 2 inverted end: " + end + " => " + (len + end));
                    end = len + end;
                }
                if (end > len) {
                    end = len;
                }
                final String str = string.substring(0, end);
                return process.getBuiltins().string(str);
            } else if (second == JNull.NULL) {
                // System.err.println("case 3");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = string.length();
                return process.getBuiltins().string(string.substring(start, end));
            } else {
                // System.err.println("case 4");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = JList.toIndex(arguments.getInt(1).getAsInt(), len);
                final String str = string.substring(Math.min(start, end), Math.max(start, end));
                if (end < start) {
                    return process.getBuiltins().string(new StringBuilder(str).reverse().toString());
                }
                return process.getBuiltins().string(str);
            }
        }
        /*
        LATER: auch hier ein stepping unterstützen
        if (arguments.size() == 3) {

        }
         */
        throw new JanitorArgumentException(process, "invalid arguments: " + arguments);
    }

    public static JFloat toFloat(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if ("".equals(string) || string.isBlank()) {
            return process.getBuiltins().floatingPoint(0);
        }
        try {
            final double iv = Double.parseDouble(string);
            return process.getBuiltins().floatingPoint(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(process, "invalid value for toFloat conversion: '" + string + "': " + e.getMessage());
        }
    }


    public static JInt toInt(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if ("".equals(string) || string.isBlank()) {
            return process.getBuiltins().integer(0);
        }
        try {
            final long iv = Long.parseLong(string, 10);
            return process.getBuiltins().integer(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(process, "invalid value for toInt conversion: '" + string + "': " + e.getMessage());
        }
    }

    public static JString toUpperCase(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(self.janitorGetHostValue().toUpperCase(Locale.ROOT));
    }

    public static JString toLowerCase(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(self.janitorGetHostValue().toLowerCase(Locale.ROOT));
    }

    public static JInt count(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().integer(StringHelpers.countMatches(self.janitorGetHostValue(), arguments.require(1).getString(0).janitorGetHostValue()));
    }

    public static JString replace(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replace(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return process.getBuiltins().string(result);
    }

    public static JString replaceFirst(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceFirst(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return process.getBuiltins().string(result);
    }

    public static JString replaceAll(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceAll(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return process.getBuiltins().string(result);
    }



    public static JanitorObject empty(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return Janitor.toBool(self.janitorGetHostValue().isEmpty());
    }

    public static JBool contains(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return Janitor.toBool(self.janitorGetHostValue().contains(arguments.getString(0).janitorGetHostValue()));
    }

    public static JBool containsIgnoreCase(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return Janitor.toBool(self.janitorGetHostValue().toLowerCase(Locale.GERMANY)
            .contains(arguments.getString(0).janitorGetHostValue().toLowerCase(Locale.GERMANY)));
    }


    public static JList splitLines(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final ArrayList<JanitorObject> list = new ArrayList<>();
        for (final String s : self.janitorGetHostValue().split("\r?\n\r?")) {
            list.add(process.getBuiltins().nullableString(s));
        }
        return process.getBuiltins().list(list);
    }

    public static JBool endsWith(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            final String string = self.janitorGetHostValue();
            return Janitor.toBool(string != null && string.endsWith(with.janitorToString()));
        }
    }

    private static final Pattern NUMBERS_ONLY = Pattern.compile("^\\d+$");
    private static final Pattern NUMBERS_AT_THE_START = Pattern.compile("^\\d+.*");

    public static JBool startsWithNumbers(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return Janitor.toBool(string != null && NUMBERS_AT_THE_START.matcher(string).matches());
    }

    public static JBool isNumeric(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return Janitor.toBool(string != null && NUMBERS_ONLY.matcher(string).matches());
    }

    public static JBool startsWith(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            final String string = self.janitorGetHostValue();
            return Janitor.toBool(string != null && string.startsWith(with.janitorGetHostValue()));
        }
    }

    public static JInt indexOf(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().integer(self.janitorGetHostValue().indexOf(arguments.getString(0).janitorGetHostValue()));
    }

    public static JInt lastIndexOf(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().integer(self.janitorGetHostValue().lastIndexOf(arguments.getString(0).janitorGetHostValue()));
    }


    public static JString substring(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final int from = (int) arguments.getInt(0).getValue();
        final int to = arguments.size() > 1 ? (int) arguments.getInt(1).getValue() : self.janitorGetHostValue().length();
        return process.getBuiltins().string(self.janitorGetHostValue().substring(from, to));
    }

    public static JString removeLeadingZeros(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        var s = self.janitorGetHostValue();
        while (s.startsWith("0")) {
            s = s.substring(1);
        }
        return process.getBuiltins().string(s);
    }


    public static JanitorObject parseDate(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return  process.getBuiltins().parseNullableDate(process, self.janitorGetHostValue(), format);
    }

    public static JanitorObject parseDateTime(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return JDateTime.parse(process, self.janitorGetHostValue(), format);
    }

    public static JanitorObject cutFilename(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        // TODO: remove this from the language, as it is only of interest for a single application!
        arguments.require(1);
        return process.getBuiltins().string(cutFilename(self.janitorGetHostValue(), arguments.getInt(0).getAsInt()));
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


    public static JanitorObject urlEncode(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(URLEncoder.encode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    public static JanitorObject urlDecode(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(URLDecoder.decode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    public static JanitorObject decodeBase64(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().binary(Base64.getDecoder().decode(self.janitorGetHostValue()));
    }

    public static JString toConstantCase(final JString self, final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        args.require(0);
        return process.getBuiltins().string(toConstant(self.janitorGetHostValue()));
    }

    public static JString toCamelCase(final JString self, final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        args.require(0);
        return process.getBuiltins().string(camelize(self.janitorGetHostValue()));
    }


    public static String toConstant(final String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        var name = Arrays.stream(string.toUpperCase().replaceAll("[^_A-Z0-9]+|_+", "_").split("_"))
                .map(s -> s.toUpperCase(Locale.ROOT))
                .reduce((s1, s2) -> s1 + "_" + s2)
                .orElse("");
        if (Character.isDigit(name.charAt(0))) {
            return "_" + name;
        } else {
            return name;
        }
    }

    public static String camelize(final String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        // alt: return CaseUtils.toCamelCase(string.toUpperCase().replaceAll("[^_A-Z0-9]+|_+", "_"), false, '_');
        final String fullCamel = Arrays.stream(string.toUpperCase().replaceAll("[^_A-Z0-9]+|_+", "_").split("_"))
                .map(s -> s.toLowerCase(Locale.ROOT))
                .map(s -> s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1))
                .reduce((s1, s2) -> s1 + s2)
                .orElse("");

        return fullCamel.substring(0, 1).toLowerCase(Locale.ROOT) + fullCamel.substring(1);
    }

    public static JanitorObject split(JString self, JanitorScriptProcess process, JCallArgs args) throws JanitorRuntimeException {
        final JanitorObject splitBy = args.require(1).get(0);
        if (splitBy instanceof JRegex regex) {
            final JList list = process.getBuiltins().list();
            final String[] parts = self.janitorGetHostValue().split(regex.janitorGetHostValue().pattern());
            for (final String part : parts) {
                list.add(process.getBuiltins().string(part));
            }
            return list;
        }
        if (splitBy instanceof JString str) {
            if (str.isEmpty()) { // special case: empty string should split all unicode chars separately
                final JList list = process.getBuiltins().list();
                for (int i = 0; i < self.janitorGetHostValue().length(); i++) {
                    list.add(process.getBuiltins().string(self.janitorGetHostValue().substring(i, i + 1)));
                }
                return list;
            } else { // usual case, split by the string, NOT interpreting it as a pattern
                final JList list = process.getBuiltins().list();
                final String[] parts = self.janitorGetHostValue().split(Pattern.quote(str.janitorGetHostValue()));
                for (final String part : parts) {
                    list.add(process.getBuiltins().string(part));
                }
                return list;
            }
        }
        return null;
    }
}
