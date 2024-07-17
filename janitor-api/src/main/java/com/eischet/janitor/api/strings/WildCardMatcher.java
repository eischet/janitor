package com.eischet.janitor.api.strings;

/**
 * A functional interface for matching strings against a wildcard pattern ("*" is for as much text as needed).
 * Janitor offers the syntax "string ~ 'foo*' for this.
 */
public interface WildCardMatcher {
    /**
     * Check if the given string matches the wildcard pattern.
     * @param testString the string to test
     * @return true if the string matches the pattern, false otherwise
     */
    boolean matches(final String testString);
}
