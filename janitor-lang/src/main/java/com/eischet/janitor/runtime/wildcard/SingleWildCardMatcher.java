// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime.wildcard;

import com.eischet.janitor.api.util.WildCardMatcher;

import java.util.regex.Pattern;

/**
 * Matches strings against a wildcard pattern, as used by the operators {@code ~} and {@code !~}: {@code *} stands for any
 * number of characters, {@code ?} for exactly one character, and every other character stands for itself (so {@code .},
 * {@code (} or {@code [} have no special meaning). The pattern must match the whole string, ignoring upper and lower case.
 * An empty pattern matches only the empty string; {@code *} matches every string.
 */
public class SingleWildCardMatcher implements WildCardMatcher {
    private final Pattern pattern;

    /**
     * Create a new SingleWildCardMatcher.
     * @param rule the wildcard pattern; null is treated like an empty pattern
     */
    public SingleWildCardMatcher(final String rule) {
        pattern = Pattern.compile(turnIntoRegex(rule == null ? "" : rule), Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /**
     * Turn a wildcard pattern into a regular expression that matches the same strings.
     * @param rule the wildcard pattern
     * @return the regex
     */
    private static String turnIntoRegex(final String rule) {
        final StringBuilder regex = new StringBuilder();
        final StringBuilder literal = new StringBuilder();
        for (int i = 0; i < rule.length(); i++) {
            final char c = rule.charAt(i);
            if (c == '*' || c == '?') {
                if (!literal.isEmpty()) {
                    regex.append(Pattern.quote(literal.toString()));
                    literal.setLength(0);
                }
                regex.append(c == '*' ? ".*" : ".");
            } else {
                literal.append(c);
            }
        }
        if (!literal.isEmpty()) {
            regex.append(Pattern.quote(literal.toString()));
        }
        return regex.toString();
    }

    @Override
    public boolean matches(final String testString) {
        return testString != null && pattern.matcher(testString).matches();
    }

}
