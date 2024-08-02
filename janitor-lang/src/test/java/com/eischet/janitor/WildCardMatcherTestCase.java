/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor;


import com.eischet.janitor.api.util.strings.MultiWildCardMatcher;
import com.eischet.janitor.api.util.strings.SingleWildCardMatcher;
import com.eischet.janitor.api.util.strings.WildCardMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the wild card matcher, which is the ~ operator in the Janitor language.
 */
public class WildCardMatcherTestCase {

    @Test
    public void testSingle() {
        final WildCardMatcher wcm = new SingleWildCardMatcher("hallo*welt");
        helloTests(wcm);
    }

    private void helloTests(final WildCardMatcher wcm) {
        assertTrue(wcm.matches("hallo, welt"));
        assertTrue(wcm.matches("Hallo, schöne neue Welt"));
        assertFalse(wcm.matches("guten tag"));
        assertFalse(wcm.matches("*"));
        assertFalse(wcm.matches("Hallo, Welt!"));
        assertFalse(wcm.matches("oh, hallo, welt"));
        assertTrue(wcm.matches("hallowelt"));
    }

    @Test
    public void testMultipleAsSingle() {
        final WildCardMatcher wcm = new MultiWildCardMatcher(List.of("hallo*welt"), false);
        helloTests(wcm);
    }

    @Test
    public void testMultiple() {
        final WildCardMatcher wcm = new MultiWildCardMatcher(List.of("hallo*welt", "foobar*"), false);
        helloTests(wcm);
        assertTrue(wcm.matches("foobar"));
        assertTrue(wcm.matches("FooBarBaz"));
        assertFalse(wcm.matches("BazFooBar"));
    }

}
