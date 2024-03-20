package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.tools.Filenames;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.runtime.types.JCallArgs;
import com.eischet.janitor.cleanup.runtime.types.JUnboundMethod;
import com.eischet.janitor.cleanup.runtime.types.JanitorClass;
import com.eischet.janitor.cleanup.template.TemplateParser;
import com.eischet.janitor.cleanup.tools.JStringUtilities;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

public class JStringClass extends JanitorClass<JString> {

    private static final ImmutableMap<String, JUnboundMethod<JString>> methods;

    static {
        final MutableMap<String, JUnboundMethod<JString>> m = Maps.mutable.empty();

        m.put("length", JStringClass::__length);
        m.put("trim", JStringClass::__trim);
        m.put("contains", JStringClass::__contains);
        m.put("containsIgnoreCase", JStringClass::__containsIgnoreCase);
        m.put("splitLines", JStringClass::__splitLines);
        m.put("indexOf", JStringClass::__indexOf);
        m.put("empty", JStringClass::__empty);
        m.put("startsWith", JStringClass::__startsWith);
        m.put("endsWith", JStringClass::__endsWith);
        m.put("removeLeadingZeros", JStringClass::__removeLeadingZeros);
        m.put("substring", JStringClass::__substring);
        m.put("replaceAll", JStringClass::__replaceAll);
        m.put("replace", JStringClass::__replace);
        m.put("replaceFirst", JStringClass::__replaceFirst);
        m.put("toUpperCase", JStringClass::__toUpperCase);
        m.put("toLowerCase", JStringClass::__toLowerCase);
        m.put("count", JStringClass::__count);
        m.put("format", JStringClass::__format);
        m.put("expand", JStringClass::__expand);
        m.put("toBinaryUtf8", JStringClass::__toBinaryUtf8);
        m.put("int", JStringClass::__toInt);
        m.put("toInt", JStringClass::__toInt);
        m.put("toFloat", JStringClass::__toFloat);
        m.put("get", JStringClass::__get);
        m.put("__get__", JStringClass::__get); // das lassen wir auch so: keine Zuweisung per Index an String-Teile, die sind ja immutable
        m.put("isNumeric", JStringClass::__isNumeric);
        m.put("startsWithNumbers", JStringClass::__startsWithNumbers);
        m.put("pureAscii", JStringClass::__pureAscii);
        m.put("simpleShortCode", JStringClass::__simpleShortCode);
        m.put("parseDate", JStringClass::__parseDate);
        m.put("parseDateTime", JStringClass::__parseDateTime);
        m.put("cutFilename", JStringClass::__cutFilename);
        m.put("urlEncode", JStringClass::__urlEncode);
        m.put("urlDecode", JStringClass::__urlDecode);
        m.put("decodeBase64", JStringClass::__decodeBase64);

        methods = m.toImmutable();
    }

    public JStringClass() {
        super(null, methods);
    }

    public static JString __format(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JString.of(self.janitorGetHostValue().formatted(arguments.getList().stream().map(JanitorObject::janitorGetHostValue).toArray()));
    }

    public static JString __expand(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return TemplateParser.expand(runningScript.getRuntime(), runningScript, self, arguments);
        // return runningScript.getRuntime().expand(runningScript, self, arguments);
        // return CSString.of(self.getHostValue().formatted(arguments.getList().stream().map(CSObj::getHostValue).toArray()));
    }

    public static JBinary __toBinaryUtf8(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return new JBinary(self.string.getBytes(StandardCharsets.UTF_8));
    }


    public static JString __get(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        if (arguments.size() == 1) {
            int len = self.string.length();
            int start = arguments.get(0) != JNull.NULL ? JList.toIndex(arguments.getInt(0).getAsInt(), len) : 0;
            return new JString(self.string.substring(start, start + 1));
        }
        if (arguments.size() == 2) {
            int len = self.string.length();
            final JanitorObject first = arguments.get(0);
            final JanitorObject second = arguments.get(1);
            // System.err.println("SUBSTRING " + first + " : " + second);
            if (first == JNull.NULL && second == JNull.NULL) {
                // System.err.println("case 1");
                return new JString(self.string);
            } else if (first == JNull.NULL) {
                // System.err.println("case 2");
                int end = arguments.getInt(1).getAsInt();
                if (end < 0) {
                    // System.err.println("case 2 inverted end: " + end + " => " + (len + end));
                    end = len + end;
                }
                final String str = self.string.substring(0, end);
                return new JString(str);
            } else if (second == JNull.NULL) {
                // System.err.println("case 3");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = self.string.length();
                return new JString(self.string.substring(start, end));
            } else {
                // System.err.println("case 4");
                int start = JList.toIndex(arguments.getInt(0).getAsInt(), len);
                int end = JList.toIndex(arguments.getInt(1).getAsInt(), len);
                final String str = self.string.substring(Math.min(start, end), Math.max(start, end));
                if (end < start) {
                    return new JString(new StringBuilder(str).reverse().toString());
                }
                return new JString(str);
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
        if ("".equals(self.string) || self.string.isBlank()) {
            return JFloat.of(0);
        }
        try {
            final double iv = Double.parseDouble(self.string);
            return JFloat.of(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(runningScript, "invalid value for toFloat conversion: '" + self.string + "': " + e.getMessage());
        }
    }


    public static JInt __toInt(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        if ("".equals(self.string) || self.string.isBlank()) {
            return JInt.of(0);
        }
        try {
            final long iv = Long.parseLong(self.string, 10);
            return JInt.of(iv);
        } catch (NumberFormatException e) {
            throw new JanitorArgumentException(runningScript, "invalid value for toInt conversion: '" + self.string + "': " + e.getMessage());
        }
    }

    public static JString __toUpperCase(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(self.string.toUpperCase(Locale.ROOT));
    }

    public static JString __toLowerCase(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(self.string.toLowerCase(Locale.ROOT));
    }

    public static JInt __count(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JInt.of(JStringUtilities.countMatches(self.janitorGetHostValue(), arguments.require(1).getString(0).janitorGetHostValue()));
    }

    public static JString __replace(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).string;
        final String with = arguments.getString(1).string;
        final String result = self.string.replace(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return JString.of(result);
    }

    public static JString __replaceFirst(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).string;
        final String with = arguments.getString(1).string;
        final String result = self.string.replaceFirst(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return JString.of(result);
    }

    public static JString __replaceAll(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(2);
        final String what = arguments.getString(0).string;
        final String with = arguments.getString(1).string;
        final String result = self.string.replaceAll(what, with);
        // System.out.println("arguments: " + arguments);
        // System.out.printf("'%s'.replaceAll('%s', '%s') --> %s%n", self.string, what, with, result);
        return JString.of(result);
    }

    public static JString __trim(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(self.string.trim());
    }

    public static JanitorObject __length(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JInt.of(self.string.length());
    }

    public static JanitorObject __empty(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JBool.map(self.string.isEmpty());
    }

    public static JBool __contains(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.map(self.janitorGetHostValue().contains(arguments.getString(0).janitorGetHostValue()));
    }

    public static JBool __containsIgnoreCase(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JBool.map(self.janitorGetHostValue().toLowerCase(Locale.GERMANY)
            .contains(arguments.getString(0).janitorGetHostValue().toLowerCase(Locale.GERMANY)));
    }


    public static JList __splitLines(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final MutableList<JanitorObject> list = Lists.mutable.empty();
        for (final String s : self.janitorGetHostValue().split("\r?\n\r?")) {
            list.add(JString.ofNullable(s));
        }
        return new JList(list.toImmutable());
    }

    public static JBool __endsWith(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            return JBool.map(self.string != null && self.string.endsWith(with.string));
        }
    }

    private static final Pattern NUMBERS_ONLY = Pattern.compile("^\\d+$");
    private static final Pattern NUMBERS_AT_THE_START = Pattern.compile("^\\d+.*");

    public static JBool __startsWithNumbers(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JBool.map(self.string != null && NUMBERS_AT_THE_START.matcher(self.string).matches());
    }

    public static JBool __isNumeric(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JBool.map(self.string != null && NUMBERS_ONLY.matcher(self.string).matches());
    }

    public static JBool __startsWith(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final JString with = arguments.require(1).getString(0);
        if (with.isEmpty()) {
            return JBool.FALSE;
        } else {
            return JBool.map(self.string != null && self.string.startsWith(with.string));
        }
    }

    public static JInt __indexOf(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return JInt.of(self.string.indexOf(arguments.getString(0).string));
    }

    public static JString __substring(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final int from = (int) arguments.getInt(0).getValue();
        final int to = arguments.size() > 1 ? (int) arguments.getInt(1).getValue() : self.string.length();
        return JString.of(self.string.substring(from, to));
    }

    public static JString __removeLeadingZeros(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        var s = self.string;
        while (s.startsWith("0")) {
            s = s.substring(1);
        }
        return JString.of(s);
    }

    public static JString __pureAscii(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(JStringUtilities.pureAscii(self.string));
    }

    public static JString __simpleShortCode(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return JString.of(JStringUtilities.simpleShortCode(self.string));
    }

    public static JanitorObject __parseDate(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).string;
        return JDate.parse(self.string, format);
    }

    public static JanitorObject __parseDateTime(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        final String format = arguments.require(1).getString(0).string;
        return JDateTime.parse(self.string, format);
    }

    public static JanitorObject __cutFilename(final JString self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        return JString.of(Filenames.cutFilename(self.janitorGetHostValue(), arguments.getInt(0).getAsInt()));
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


}
