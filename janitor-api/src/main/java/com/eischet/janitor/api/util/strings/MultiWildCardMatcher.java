package com.eischet.janitor.api.util.strings;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A WildCardMatcher that matches if any of the rules match.
 * This is not actually used within Janitor right now, but within existing client code.
 * TODO: Figure out if this is needed at all in here.
 */
public class MultiWildCardMatcher implements WildCardMatcher {

    private final List<SingleWildCardMatcher> matchers;
    private final boolean matchOnEmptyRules;

    /**
     * Create a new MultiWildCardMatcher.
     * @param rules the rules
     * @param matchOnEmptyRules whether to match on empty rules
     */
    public MultiWildCardMatcher(final Collection<String> rules, final boolean matchOnEmptyRules) {
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
