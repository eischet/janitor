package com.eischet.janitor;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.toolbox.memory.Fact;
import com.eischet.janitor.toolbox.memory.Memory;
import com.eischet.janitor.toolbox.strings.StringHelpers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases that have no real meaning but increasing code coverage.
 *
 * Also, test cases that satisfy some picker IDEs that have no way of being told: "this is for users of a library".
 * Honestly, I have no idea why there is no such annotation e.g. in IntelliJ IDEA, but it annoys me to no end.
 */
public class AdditionalCoverageTestCase {

    @Test
    public void coverage() throws JanitorRuntimeException {
        assertFalse(JanitorSemantics.isTruthy(null));

    }

    @Test
    public void stringHelpers() {
        assertEquals("f[...]", StringHelpers.cut("foo", 1));
        assertEquals("C384", StringHelpers.printHexBinary("Ä".getBytes()));
        assertTrue(StringHelpers.isEmpty(""));
        // The next line is for a special inconvenience provided by IntalliJ. Is this IDE getting crazier or do I just notice it more?
        //noinspection ConstantValue
        assertTrue(StringHelpers.isEmpty(null));
        assertFalse(StringHelpers.isEmpty("foo"));
    }

    private static class MemoryWithTimer<T> extends Memory<T> {

        private long time;

        public MemoryWithTimer(long term, TimeUnit unit) {
            super(term, unit);
        }

        @Override
        protected long now() {
            return time;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long trick() {
            return super.now();
        }
    }

    @Test
    public void memoryTest() {
        MemoryWithTimer<String> stringMemory = new MemoryWithTimer<>(10, TimeUnit.MILLISECONDS);
        stringMemory.setTime(100);
        stringMemory.remember("foo");
        List<String> snapshot1 = stringMemory.stream().toList();
        assertEquals(List.of("foo"), snapshot1);
        stringMemory.setTime(105); // advance time by 5ms
        List<String> snapshot2 = stringMemory.stream().toList();
        assertEquals(List.of("foo"), snapshot2);
        stringMemory.setTime(109); // advance time by another 4ms
        List<String> snapshot3 = stringMemory.stream().toList();
        assertEquals(List.of("foo"), snapshot3);
        stringMemory.setTime(110); // one more millisecond
        List<String> snapshot4 = stringMemory.stream().toList();
        assertEquals(List.of("foo"), snapshot4);
        stringMemory.setTime(111); // yet another one. This time, the initial 10 ms should have passed.
        List<String> snapshot5 = stringMemory.stream().toList();
        assertEquals(Collections.emptyList(), snapshot5);
        // now lets try two distinct entries

        stringMemory.setTime(1000);
        assertEquals(0, stringMemory.size());
        stringMemory.remember("me");
        assertEquals(1, stringMemory.size());
        stringMemory.setTime(1005);
        stringMemory.remember("you");
        assertEquals(2, stringMemory.size());
        assertTrue(stringMemory.contains("me") && stringMemory.contains("you"));
        stringMemory.setTime(1010);
        assertEquals(2, stringMemory.size());
        assertTrue(stringMemory.contains("me") && stringMemory.contains("you"));
        stringMemory.setTime(1011);
        assertEquals(1, stringMemory.size());
        assertTrue((!stringMemory.contains("me")) && stringMemory.contains("you"));
        stringMemory.setTime(1020);
        assertEquals(0, stringMemory.size());

        assertTrue(stringMemory.trick() > 10_000_000L);

        stringMemory.setTime(5000);
        stringMemory.remember("me");
        stringMemory.remember("me");
        stringMemory.setTime(5005);
        stringMemory.remember("me");
        stringMemory.setTime(5019);
        assertEquals(0, stringMemory.size());




    }

}
