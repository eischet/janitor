package com.eischet.janitor.api.strings;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MultiWildCardMatcher implements WildCardMatcher {

    private final List<SingleWildCardMatcher> matchers;
    private final boolean matchOnEmptyRules;

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
