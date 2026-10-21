// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.internals;


import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.runtime.wildcard.MultiWildCardMatcher;
import com.eischet.janitor.runtime.wildcard.SingleWildCardMatcher;
import com.eischet.janitor.api.util.WildCardMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the wild card matcher, which is the ~ operator in the Janitor language.
 */
public class WildCardMatcherTestCase extends JanitorTest {

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
    public void questionMarkIsOneCharacter() {
        final WildCardMatcher wcm = new SingleWildCardMatcher("a?c");
        assertTrue(wcm.matches("abc"));
        assertTrue(wcm.matches("A.C"));
        assertFalse(wcm.matches("ac"));
        assertFalse(wcm.matches("abbc"));
    }

    @Test
    public void otherCharactersAreLiteral() {
        assertTrue(new SingleWildCardMatcher("*.pdf").matches("report.pdf"));
        assertFalse(new SingleWildCardMatcher("*.pdf").matches("reportXpdf"));
        assertFalse(new SingleWildCardMatcher("a.c").matches("abc"));
        assertTrue(new SingleWildCardMatcher("(*)").matches("(1)"));
        assertTrue(new SingleWildCardMatcher("a+b").matches("a+b"));
        assertFalse(new SingleWildCardMatcher("a+b").matches("aab"));
        assertTrue(new SingleWildCardMatcher("[x]^$|").matches("[x]^$|"));
        assertTrue(new SingleWildCardMatcher("a\\E*").matches("a\\Ez")); // a regex quote end inside the pattern
    }

    @Test
    public void emptyAndStarPatterns() {
        assertTrue(new SingleWildCardMatcher("*").matches(""));
        assertTrue(new SingleWildCardMatcher("*").matches("anything"));
        assertTrue(new SingleWildCardMatcher("").matches(""));
        assertFalse(new SingleWildCardMatcher("").matches("x"));
        assertFalse(new SingleWildCardMatcher("*").matches(null));
    }

    @Test
    public void caseIsIgnoredAlsoForUmlauts() {
        final String upper = String.valueOf((char) 0xC4); // A with diaeresis
        final String lower = String.valueOf((char) 0xE4);
        assertTrue(new SingleWildCardMatcher("*.pdf").matches("REPORT.PDF"));
        assertTrue(new SingleWildCardMatcher(lower + "*").matches(upper + "rger"));
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
