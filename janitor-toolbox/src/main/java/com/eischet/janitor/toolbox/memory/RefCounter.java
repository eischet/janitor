package com.eischet.janitor.toolbox.memory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple reference counter that starts at 0.
 * Every usage (acquisition) increases the counter by 1.
 * When the counter reaches 0, the callback is executed.
 */
public class RefCounter {

    protected final AtomicLong counter = new AtomicLong(0);
    protected final Runnable onZero;
    protected boolean wasZeroed = false;

    public RefCounter(Runnable onZero) {
        if (onZero == null) throw new IllegalArgumentException("onZero runnable must not be null");
        this.onZero = onZero;
    }

    /**
     * Increase the counter.
     */
    public void acquire() {
        if (wasZeroed) {
            throw new IllegalStateException("RefCounter was zeroed before, you cannot restart it.");
        }
        counter.incrementAndGet();
    }

    /**
     * Decrease the counter.
     */
    public void release() {
        long value = counter.decrementAndGet();
        if (wasZeroed) {
            throw new IllegalStateException("RefCounter was already zeroed.");
        }
        if (value < 0) {
            throw new IllegalStateException("RefCounter released more often than acquired.");
        }
        if (value == 0) {
            wasZeroed = true;
            onZero.run();
        }
    }

    /**
     * Return the current counter value.
     *
     * @return the current counter value
     */
    public long get() {
        return counter.get();
    }
}