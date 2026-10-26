// SPDX-FileCopyrightText: 2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.Uplink;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JoinedListTestCase {

    /**
     * A parent entity in a cache is shared by all threads; when several of them load its join records at once, they must be loaded once,
     * not once per thread.
     */
    @Test
    void concurrentLoadsLoadOnce() throws Exception {
        final OrmJoined record = (OrmJoined) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{OrmJoined.class},
                (proxy, method, args) -> null);
        final AtomicInteger loads = new AtomicInteger();
        final JoinedList<OrmJoined, Uplink, JoinDao<OrmJoined>, OrmEntity> joined = new JoinedList<>(null, OrmJoined.class,
                () -> null, uplink -> null, (conn, dao) -> {
                    loads.incrementAndGet();
                    try {
                        Thread.sleep(50); // long enough for the other threads to arrive while this one loads
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return List.of(record, record);
                }, joinRecord -> null);

        final int threads = 8;
        final CountDownLatch start = new CountDownLatch(1);
        final List<Thread> started = new ArrayList<>();
        final List<Throwable> failures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final Thread thread = new Thread(() -> {
                try {
                    start.await();
                    joined.load(null);
                } catch (Throwable e) {
                    synchronized (failures) {
                        failures.add(e);
                    }
                }
            });
            thread.start();
            started.add(thread);
        }
        start.countDown();
        for (final Thread thread : started) {
            thread.join();
        }

        assertTrue(failures.isEmpty(), () -> "failures: " + failures);
        assertEquals(1, loads.get());
        assertEquals(2, joined.size());
        assertTrue(joined.isLoaded());
    }

}
