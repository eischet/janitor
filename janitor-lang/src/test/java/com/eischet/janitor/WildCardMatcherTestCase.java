/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor;


import com.eischet.janitor.api.strings.MultiWildCardMatcher;
import com.eischet.janitor.api.strings.SingleWildCardMatcher;
import com.eischet.janitor.api.strings.WildCardMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    String replaceConsecutiveNumbers(String input) {
        // In the input string, replace all consecutive numbers with "X", except for the first two digits. Do nothing for numbers prefixed with "SYD-".
        return input.replaceAll("(?<!SYD-)(\\d+)\\d{4}", "XX$1");
    }
    /*


    // Write a regeular expression that replaces numbers with "X" that are not prefixed with "SYD-", keeping the first four digits
    String repl(String s) {
        return replaceNumbers.matcher(s).replaceAll("X");
    }


    Pattern replaceNumbers = Pattern.compile("(?<!SYD-)\\d+");
*/
}
