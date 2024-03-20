package com.eischet.janitor.cleanup.tools;

import net.gcardone.junidecode.Junidecode;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.collections.api.collection.ImmutableCollection;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JStringUtilities {

    public static String repeat(final String s, final long num) {
        final StringBuilder repetitions = new StringBuilder();
        for (long i = 0; i < num; i++) {
            repetitions.append(s);
        }
        return repetitions.toString();
    }

    public static final int INDEX_NOT_FOUND = -1;

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
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

    public static String unescapeJava(final String literal) {
        return StringEscapeUtils.unescapeJava(literal);
    }

    public static String pureAscii(final String string) {
        return Junidecode.unidecode(string.replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("Ä", "Ae")
            .replace("Ö", "Oe")
            .replace("Ü", "Ue")
            .replace("ß", "ss")
);
        // ICU4J wiegt 8 MB: return germanTransliterator.transliterate(string);
    }

    public static String simpleShortCode(final String string) {
        return pureAscii(string).toUpperCase(Locale.GERMANY)
            .trim()
            .replaceAll("[\\r\\n ]+", " ")
            .replace("&", "")
            .replace("^", "")
            .replace("`", "")
            .replace("´", "")
            .replace("%", "")
            .replace("$", "")
            .replace("\\", "")
            .replace("'", "")
            .replace("\"", "")
            .replace(".", "")
            .replace(":", "")
            .replace(",", "")
            .replace("(", "")
            .replace(")", "")
            .replace("=", "")
            .replace("+", "")
            .replace("?", "")
            .replace("/", "_")
            .replace(" ", "_")
            .replace("-", "_")
            .replaceAll("_+", "_")
            ;
    }

    public static String base64encode(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64decode(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
    }


    public interface WildCardMatcher {
        boolean matches(final String testString);
    }

    public static class MultiWildCardMatcher implements WildCardMatcher {

        private final List<SingleWildCardMatcher> matchers;
        private final boolean matchOnEmptyRules;

        public MultiWildCardMatcher(final ImmutableCollection<String> rules, final boolean matchOnEmptyRules) {
            this.matchOnEmptyRules = matchOnEmptyRules;
            if (rules == null || rules.isEmpty()) {
                matchers = null;
            } else {
                matchers = rules.stream().map(SingleWildCardMatcher::new).collect(Collectors.toList());
            }
        }

        @Override
        public boolean matches(final String testString) {
            if (matchers == null || matchers.isEmpty()) {
                return matchOnEmptyRules;
            } else {
                return matchers.stream().anyMatch(m -> m.matches(testString));
            }
        }
    }

    public static class SingleWildCardMatcher implements WildCardMatcher {
        private final Pattern pattern;

        public SingleWildCardMatcher(final String rule) {
            if (rule == null || rule.isEmpty() || Objects.equals("*", rule)) {
                pattern = null;
            } else {
                pattern = Pattern.compile(turnIntoRegex(rule), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            }
        }

        private static String turnIntoRegex(final String rule) {
            return "^" + rule.replace("*", ".*") + "$";
        }

        @Override
        public boolean matches(final String testString) {
            return testString != null && pattern.matcher(testString).matches();
        }

    }


}
