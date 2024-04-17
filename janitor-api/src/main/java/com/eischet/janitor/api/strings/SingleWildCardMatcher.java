package com.eischet.janitor.api.strings;

import java.util.Objects;
import java.util.regex.Pattern;

public class SingleWildCardMatcher implements WildCardMatcher {
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
