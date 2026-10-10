package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.toolbox.strings.StringHelpers;
import org.jetbrains.annotations.NotNull;
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
 *
 * {@see com.eischet.janitor.env.DefaultBuiltinTypes}
 */
public class JStringClass {

    public static final String STRING_CLASS = """
            The String class represents sequences of characters.
    
            String instances themselves are immutable, i.e. they cannot be changed.
            But you can call methods that return a different String.
            
            """;

    public static final String STRING_LENGTH = "String.length(): Returns the number of characters in the string.";

    /**
     * Script method {@code string.length()}: the number of characters in the string.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the length
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JanitorObject length(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().integer(self.janitorGetHostValue().length());
    }

    public static final String STRING_TRIM = "String.trim(): Returns the String with leading and trailing spaces removed";

    /**
     * Script method {@code string.trim()}: removes leading and trailing whitespace.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the trimmed string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString trim(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(self.janitorGetHostValue().trim());
    }

    /**
     * Script method {@code string.format(args...)}: formats the string as a Java format string with the arguments.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the formatted string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JString format(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().string(self.janitorGetHostValue().formatted(arguments.requireArgListOnly().stream().map(JanitorObject::janitorGetHostValue).toArray()));
    }

    /**
     * Script method {@code string.expand(map)}: expands a template, i.e. replaces placeholders such as {@code ${name}} by the values from the map.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the expanded string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JString expand(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        return process.expandTemplate(self, arguments);
    }

    /**
     * Script method {@code string.toBinaryUtf8()}: encodes the string as UTF-8.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the binary data
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JBinary toBinaryUtf8(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().binary(self.janitorGetHostValue().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Script method {@code string.encode(charset)}: encodes the string with a character set, UTF-8 by default.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the binary data
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JBinary encode(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final String enc = arguments.getOptionalStringValue(0, "UTF-8");
        try {
            return process.getBuiltins().binary(self.janitorGetHostValue().getBytes(enc));
        } catch (UnsupportedEncodingException e) {
            throw new JanitorNativeException(process, "invalid binary encoding: " + enc, e);
        }
    }


    /**
     * Index/slice a string, Python-style. Mirrors JList's indexing rules (see
     * JListPythonIndexingTestCase/JListTestCase/JListSteppedSliceTestCase) minus assignment, since
     * strings are immutable: single-character access ({@code s[i]}) is strict -- the index must
     * reference an existing character, or this throws, exactly like Python's IndexError. Slicing
     * ({@code s[a:b]}, with or without a step) is forgiving -- out-of-range bounds are silently
     * clamped into {@code [0, length()]}, and a descending range with the default step of 1 reads as
     * an empty string, not reversed (use a negative step, e.g. {@code s[::-1]}, to reverse a string).
     */
    public static @NotNull JString indexedGet(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        final int len = string.length();
        if (arguments.size() == 1) {
            final int rawIndex = arguments.getInt(0).getAsInt();
            final int resolved = JList.toIndex(rawIndex, len);
            if (resolved < 0 || resolved >= len) {
                throw new JanitorArgumentException(process, "string index " + rawIndex + " out of range for string of length " + len);
            }
            return process.getBuiltins().string(string.substring(resolved, resolved + 1));
        }
        if (arguments.size() == 2) {
            final int start = arguments.get(0) == JNull.NULL ? 0 : clamp(JList.toIndex(arguments.getInt(0).getAsInt(), len), 0, len);
            final int end = arguments.get(1) == JNull.NULL ? len : clamp(JList.toIndex(arguments.getInt(1).getAsInt(), len), 0, len);
            if (end <= start) {
                return Janitor.emptyString();
            }
            return process.getBuiltins().string(string.substring(start, end));
        }
        if (arguments.size() == 3) {
            final Integer startArg = arguments.get(0) == JNull.NULL ? null : arguments.getInt(0).getAsInt();
            final Integer endArg = arguments.get(1) == JNull.NULL ? null : arguments.getInt(1).getAsInt();
            final int step = arguments.getInt(2).getAsInt();
            if (step == 0) {
                throw new JanitorArgumentException(process, "slice step cannot be zero");
            }
            final int startIndex;
            final int endIndex;
            if (step > 0) {
                startIndex = startArg == null ? 0 : clamp(JList.toIndex(startArg, len), 0, len);
                endIndex = endArg == null ? len : clamp(JList.toIndex(endArg, len), 0, len);
            } else {
                startIndex = startArg == null ? len - 1 : clamp(JList.toIndex(startArg, len), -1, len - 1);
                endIndex = endArg == null ? -1 : clamp(JList.toIndex(endArg, len), -1, len - 1);
            }
            final StringBuilder result = new StringBuilder();
            if (step > 0) {
                for (int i = startIndex; i < endIndex; i += step) {
                    result.append(string.charAt(i));
                }
            } else {
                for (int i = startIndex; i > endIndex; i += step) {
                    result.append(string.charAt(i));
                }
            }
            return process.getBuiltins().string(result.toString());
        }
        throw new JanitorArgumentException(process, "invalid arguments: " + arguments);
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Script method {@code string.toFloat()}: converts the string to a float; a blank string is converted to 0.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JFloat toFloat(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final String string = self.janitorGetHostValue();
        if (string.isBlank()) {
            return process.getBuiltins().floatingPoint(0);
        }
        try {
            final double iv = Double.parseDouble(string);
            return process.getBuiltins().floatingPoint(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(process, "invalid value for toFloat conversion: '" + string + "': " + e.getMessage());
        }
    }


    /**
     * Script method {@code string.toInt()}: converts the string to an integer; a blank string is converted to 0.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JInt toInt(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.notAllowed();
        final String string = self.janitorGetHostValue();
        if (string.isBlank()) {
            return process.getBuiltins().integer(0);
        }
        try {
            final long iv = Long.parseLong(string, 10);
            return process.getBuiltins().integer(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(process, "invalid value for toInt conversion: '" + string + "': " + e.getMessage());
        }
    }

    /**
     * Script method {@code string.toUpperCase()}: converts the string to upper case.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the converted string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JString toUpperCase(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(self.janitorGetHostValue().toUpperCase(Locale.ROOT));
    }

    /**
     * Script method {@code string.toLowerCase()}: converts the string to lower case.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the converted string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JString toLowerCase(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return process.getBuiltins().string(self.janitorGetHostValue().toLowerCase(Locale.ROOT));
    }

    /**
     * Script method {@code string.count(text)}: counts how often a text occurs in the string.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number of occurrences
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static @NotNull JInt count(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().integer(StringHelpers.countMatches(self.janitorGetHostValue(), arguments.require(1).getString(0).janitorGetHostValue()));
    }

    /**
     * Script method {@code string.replace(what, with)}: replaces all occurrences of a literal text.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the new string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString replace(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replace(what, with);
        return process.getBuiltins().string(result);
    }

    /**
     * Script method {@code string.replaceFirst(regex, with)}: replaces the first match of a regular expression.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the new string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString replaceFirst(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceFirst(what, with);
        return process.getBuiltins().string(result);
    }

    /**
     * Script method {@code string.replaceAll(regex, with)}: replaces all matches of a regular expression.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the new string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString replaceAll(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).janitorGetHostValue();
        final String with = arguments.getString(1).janitorGetHostValue();
        final String result = self.janitorGetHostValue().replaceAll(what, with);
        return process.getBuiltins().string(result);
    }



    /**
     * Script method {@code string.empty()}: checks whether the string has no characters.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the string is empty
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject empty(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return Janitor.toBool(self.janitorGetHostValue().isEmpty());
    }

    /**
     * Script method {@code string.contains(text)}: checks whether the string contains a text.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the text was found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool contains(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return Janitor.toBool(self.janitorGetHostValue().contains(arguments.getString(0).janitorGetHostValue()));
    }

    /**
     * Script method {@code string.containsIgnoreCase(text)}: checks whether the string contains a text, ignoring case.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the text was found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool containsIgnoreCase(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return Janitor.toBool(self.janitorGetHostValue().toLowerCase(Locale.GERMANY)
            .contains(arguments.getString(0).janitorGetHostValue().toLowerCase(Locale.GERMANY)));
    }


    /**
     * Script method {@code string.splitLines()}: splits the string into lines, accepting both Unix and Windows line breaks.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return a list of the lines
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JList splitLines(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final ArrayList<JanitorObject> list = new ArrayList<>();
        for (final String s : self.janitorGetHostValue().split("\r?\n\r?")) {
            list.add(process.getBuiltins().nullableString(s));
        }
        return process.getBuiltins().list(list);
    }

    /**
     * Script method {@code string.endsWith(text)}: checks whether the string ends with a text. An empty text never matches.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the string ends with the text
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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

    /**
     * Script method {@code string.startsWithNumbers()}: checks whether the string starts with a digit.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the string starts with a digit
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool startsWithNumbers(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return Janitor.toBool(string != null && NUMBERS_AT_THE_START.matcher(string).matches());
    }

    /**
     * Script method {@code string.isNumeric()}: checks whether the string consists of digits only.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the string is numeric
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool isNumeric(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        final String string = self.janitorGetHostValue();
        return Janitor.toBool(string != null && NUMBERS_ONLY.matcher(string).matches());
    }

    /**
     * Script method {@code string.startsWith(text)}: checks whether the string starts with a text. An empty text never matches.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the string starts with the text
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool startsWith(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            final String string = self.janitorGetHostValue();
            return Janitor.toBool(string != null && string.startsWith(with.janitorGetHostValue()));
        }
    }

    /**
     * Script method {@code string.indexOf(text)}: finds the first occurrence of a text.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the index, or -1 if the text was not found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JInt indexOf(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().integer(self.janitorGetHostValue().indexOf(arguments.getString(0).janitorGetHostValue()));
    }

    /**
     * Script method {@code string.lastIndexOf(text)}: finds the last occurrence of a text.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the index, or -1 if the text was not found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JInt lastIndexOf(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return process.getBuiltins().integer(self.janitorGetHostValue().lastIndexOf(arguments.getString(0).janitorGetHostValue()));
    }


    /**
     * Script method {@code string.substring(from, to)}: extracts a part of the string. The end index is optional.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the substring
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString substring(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final int from = (int) arguments.getInt(0).getValue();
        final int to = arguments.size() > 1 ? (int) arguments.getInt(1).getValue() : self.janitorGetHostValue().length();
        return process.getBuiltins().string(self.janitorGetHostValue().substring(from, to));
    }

    /**
     * Script method {@code string.removeLeadingZeros()}: removes all zeros at the start of the string.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the new string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString removeLeadingZeros(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        var s = self.janitorGetHostValue();
        while (s.startsWith("0")) {
            s = s.substring(1);
        }
        return process.getBuiltins().string(s);
    }


    /**
     * Script method {@code string.parseDate(pattern)}: parses the string as a date.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the date
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject parseDate(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return  process.getBuiltins().parseNullableDate(process, self.janitorGetHostValue(), format);
    }

    /**
     * Script method {@code string.parseDateTime(pattern)}: parses the string as a datetime.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the datetime
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject parseDateTime(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).janitorGetHostValue();
        return JDateTime.parse(process, self.janitorGetHostValue(), format);
    }

    /**
     * Script method {@code string.cutFilename(maxLength)}: reduces a file name to the given length, preserving the file extension.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the shortened file name
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject cutFilename(final JString self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        // TODO: remove this from the language, as it is only of interest for a single application!
        arguments.require(1);
        return process.getBuiltins().string(cutFilename(self.janitorGetHostValue(), arguments.getInt(0).getAsInt()));
    }

    /**
     * Reduces a file name to the given length, preserving the file extension.
     * @param filename the file name or path, may be null
     * @param maxLength the maximum length of the result
     * @return the shortened file name, or null if the file name is null
     */
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


    /**
     * Script method {@code string.urlEncode()}: encodes the string for use in a URL.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the encoded string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject urlEncode(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.notAllowed();
        return process.getBuiltins().string(URLEncoder.encode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    /**
     * Script method {@code string.urlDecode()}: decodes a string that was encoded for use in a URL.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the decoded string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject urlDecode(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.notAllowed();
        return process.getBuiltins().string(URLDecoder.decode(self.janitorGetHostValue(), StandardCharsets.UTF_8));
    }

    /**
     * Script method {@code string.decodeBase64()}: decodes a base64 string.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the decoded binary data
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject decodeBase64(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        arguments.notAllowed();
        return process.getBuiltins().binary(Base64.getDecoder().decode(self.janitorGetHostValue()));
    }

    /**
     * Script method {@code string.toConstantCase()}: converts the string to an upper case constant name, e.g. "fooBar baz" to "FOOBAR_BAZ".
     * @param self the object that the method is called on
     * @param process the running script process
     * @param args the call arguments
     * @return the converted string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString toConstantCase(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs args) throws JanitorRuntimeException {
        args.notAllowed();
        if (self.janitorGetHostValue().isBlank()) {
            return Janitor.emptyString();
        }
        return process.getBuiltins().string(toConstant(self.janitorGetHostValue()));
    }

    /**
     * Script method {@code string.toCamelCase()}: converts the string to camel case, e.g. "FOO_BAR" to "fooBar".
     * @param self the object that the method is called on
     * @param process the running script process
     * @param args the call arguments
     * @return the converted string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString toCamelCase(final @NotNull JString self, final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs args) throws JanitorRuntimeException {
        args.notAllowed();
        if (self.janitorGetHostValue().isBlank()) {
            return Janitor.emptyString();
        }
        return process.getBuiltins().string(camelize(self.janitorGetHostValue()));
    }


    /**
     * Converts a string to an upper case constant name, with underscores between the words.
     * @param string the string, may be null
     * @return the constant name, or null if the string is null or empty
     */
    public static @Nullable String toConstant(final @Nullable String string) {
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

    /**
     * Converts a string to camel case.
     * @param string the string, may be null
     * @return the camel case string, or null if the string is null or empty
     */
    public static @Nullable String camelize(final @Nullable String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        // old: return CaseUtils.toCamelCase(string.toUpperCase().replaceAll("[^_A-Z0-9]+|_+", "_"), false, '_');
        final String fullCamel = Arrays.stream(string.toUpperCase().replaceAll("[^_A-Z0-9]+|_+", "_").split("_"))
                .map(s -> s.toLowerCase(Locale.ROOT))
                .map(s -> s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1))
                .reduce((s1, s2) -> s1 + s2)
                .orElse("");

        return fullCamel.substring(0, 1).toLowerCase(Locale.ROOT) + fullCamel.substring(1);
    }

    /**
     * Script method {@code string.split(separator)}: splits the string at a separator, which is either a string or a regular expression.
     * An empty separator splits the string into single characters.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param args the call arguments
     * @return a list of the parts
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
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
        // TODO: shouldn't we throw a script exception here?
        return null;
    }

    /**
     * Registers the standard methods and properties of strings.
     * @param stringDispatcher the dispatch table to add them to
     */
    public static void applyDefaults(DispatchTable<JString> stringDispatcher) {
                stringDispatcher.setMetaData(Janitor.MetaData.HELP, JStringClass.STRING_CLASS);
        // OLD: addStringMethod("length", JStringClass::__length);
        stringDispatcher.addMethod("length", JStringClass::length)
                .setMetaData(Janitor.MetaData.HELP, JStringClass.STRING_LENGTH); // "foo".length() == 3
        stringDispatcher.addMethod("trim", JStringClass::trim)
                .setMetaData(Janitor.MetaData.HELP, JStringClass.STRING_TRIM); // "  foo  ".trim() == "foo"
        stringDispatcher.addMethod("contains", JStringClass::contains); // "foobar".contains("bar") == true, "barbaz".contains("foo") == false
        stringDispatcher.addMethod("containsIgnoreCase", JStringClass::containsIgnoreCase); // "foobar".containsIgnoreCase("BAR") == true
        stringDispatcher.addMethod("splitLines", JStringClass::splitLines); // "foo\nbar\nbaz".splitLines() == ["foo", "bar", "baz"]
        stringDispatcher.addMethod("indexOf", JStringClass::indexOf); // "foobar".indexOf("bar") == 3, "foobar".indexOf("x") == -1
        stringDispatcher.addMethod("lastIndexOf", JStringClass::lastIndexOf); // "foobar".indexOf("bar") == 3, "foobar".indexOf("x") == -1
        stringDispatcher.addMethod("empty", JStringClass::empty); // "".empty() == true, "foo".empty() == false
        stringDispatcher.addMethod("startsWith", JStringClass::startsWith); // "foobar".startsWith("foo") == true, "foobar".startsWith("bar") == false
        stringDispatcher.addMethod("endsWith", JStringClass::endsWith); // "foobar".endsWith("bar") == true, "foobar".endsWith("foo") == false
        stringDispatcher.addMethod("removeLeadingZeros", JStringClass::removeLeadingZeros); // "000123".removeLeadingZeros() == "123"
        stringDispatcher.addMethod("substring", JStringClass::substring); // "foobar".substring(3) == "bar", "foobar".substring(3, 5) == "ba"
        stringDispatcher.addMethod("replaceAll", JStringClass::replaceAll); // "foobar".replaceAll("o", "x") == "fxxbar"
        stringDispatcher.addMethod("replace", JStringClass::replace); // "foobar".replace("o", "x") == "fxobar"
        stringDispatcher.addMethod("replaceFirst", JStringClass::replaceFirst); // "foobar".replaceFirst("o", "x") == "fxobar"
        stringDispatcher.addMethod("toUpperCase", JStringClass::toUpperCase); // "foo".toUpperCase() == "FOO"
        stringDispatcher.addMethod("toLowerCase", JStringClass::toLowerCase); // "FOO".toLowerCase() == "foo"
        stringDispatcher.addMethod("count", JStringClass::count); // "foobar".count("o") == 2
        stringDispatcher.addMethod("format", JStringClass::format); // "Hello, %s!".format("world") == "Hello, world!"
        stringDispatcher.addMethod("expand", JStringClass::expand); // "Hello, ${name}!".expand({name: "world"}) == "Hello, world!"
        stringDispatcher.addMethod("toBinaryUtf8", JStringClass::toBinaryUtf8); // convert to binary, in UTF-8
        stringDispatcher.addMethod("encode", JStringClass::encode); // convert to binary, in the given character set
        stringDispatcher.addMethod("int", JStringClass::toInt); // "123".int() == 123
        stringDispatcher.addMethod("toInt", JStringClass::toInt); // "123".toInt() == 123
        stringDispatcher.addMethod("toFloat", JStringClass::toFloat); // "123.45".toFloat() == 123.45
        stringDispatcher.addMethod("get", JStringClass::indexedGet); // "foobar".get(3) == "b", "foobar".get(3, 5) == "ba"
        stringDispatcher.addMethod("isNumeric", JStringClass::isNumeric); // "17".isNumeric() == true, "mario".isNumeric() == false
        stringDispatcher.addMethod("startsWithNumbers", JStringClass::startsWithNumbers); // "123foo".startsWithNumbers() == true, "foo123".startsWithNumbers() == false
        stringDispatcher.addMethod("parseDate", JStringClass::parseDate); // "2021-12-31".parseDate('yyyy-MM-dd') == @2021-12-31
        stringDispatcher.addMethod("parseDateTime", JStringClass::parseDateTime); // "2021-12-31T23:59:59".parseDateTime('yyyy-MM-dd\'T\'HH:mm:ss') == @2021-12-31-23:59:59
        stringDispatcher.addMethod("split", JStringClass::split); // "foo,bar,baz".split(",") == ["foo", "bar", "baz"]
        stringDispatcher.addMethod("cutFilename", JStringClass::cutFilename);
        stringDispatcher.addMethod("urlEncode", JStringClass::urlEncode);
        stringDispatcher.addMethod("urlDecode", JStringClass::urlDecode);
        stringDispatcher.addMethod("decodeBase64", JStringClass::decodeBase64);
        stringDispatcher.addMethod("toCamelCase", JStringClass::toCamelCase);
        stringDispatcher.addMethod("toConstantCase", JStringClass::toConstantCase);
        stringDispatcher.addMethod(JanitorAntlrCompiler.INDEXED_GET_METHOD, JStringClass::indexedGet); // leave this as it is: no assignment by index to parts of a string, since strings are immutable
    }

}
