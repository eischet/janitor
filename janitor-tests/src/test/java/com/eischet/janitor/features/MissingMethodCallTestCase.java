// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Calling a method that does not exist must fail with an error the script can catch, and never hang the host: the
 * instruction limit cannot stop a loop inside the interpreter itself.
 */
@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class MissingMethodCallTestCase extends JanitorTest {

    @Test
    void onAnEmptyMap() {
        final JanitorNameException e = assertThrows(JanitorNameException.class, () -> evaluate("return {}.foo();"));
        assertTrue(e.getText().contains("foo"), e.getText());
    }

    @Test
    void onAMapWithOtherKeys() {
        assertThrows(JanitorNameException.class, () -> evaluate("m = {a: 1}; return m.foo(1, 2);"));
    }

    @Test
    void onNull() {
        final JanitorNameException e = assertThrows(JanitorNameException.class, () -> evaluate("x = null; return x.foo();"));
        assertTrue(e.getText().contains("foo"), e.getText());
    }

    @Test
    void onAMapValueThatIsNotCallable() {
        assertThrows(JanitorNameException.class, () -> evaluate("m = {foo: 1}; return m.foo();"));
    }

    @Test
    void scriptsCanCatchIt() throws Exception {
        assertEquals("caught", evaluate("try { {}.foo(); } catch (e) { return 'caught'; }").janitorToString());
    }

    @Test
    void mapValuesThatAreCallableStillWork() throws Exception {
        assertEquals("3", evaluate("m = {add: (a, b) -> a + b}; return m.add(1, 2);").janitorToString());
    }

    @Test
    void guardedCallOnNullReturnsNullWithoutEvaluatingArguments() throws Exception {
        assertEquals("[true, 0]", evaluate("""
                calls = 0;
                count = () -> { calls++; return calls; };
                x = null;
                return [x?.foo(count()) == null, calls];
                """).janitorToString());
    }}
