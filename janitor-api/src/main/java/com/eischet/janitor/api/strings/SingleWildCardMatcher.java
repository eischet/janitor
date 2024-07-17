package com.eischet.janitor.api.strings;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A simple implementation of a WildCardMatcher.
 */
public class SingleWildCardMatcher implements WildCardMatcher {
    private final Pattern pattern;

    /**
     * Create a new SingleWildCardMatcher.
     * @param rule the rule
     */
    public SingleWildCardMatcher(final String rule) {
        if (rule == null || rule.isEmpty() || Objects.equals("*", rule)) {
            pattern = null;
        } else {
            pattern = Pattern.compile(turnIntoRegex(rule), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * Turn a rule into a regex.
     * @param rule the rule
     * @return the regex
     */
    private static String turnIntoRegex(final String rule) {
        return "^" + rule.replace("*", ".*") + "$";
    }

    @Override
    public boolean matches(final String testString) {
        return testString != null && pattern.matcher(testString).matches();
    }

}
