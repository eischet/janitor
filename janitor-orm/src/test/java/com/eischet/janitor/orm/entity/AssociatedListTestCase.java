// SPDX-FileCopyrightText: 2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.meta.EntityWrangler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssociatedListTestCase {

    private final OrmEntity record = proxy(OrmEntity.class, (proxy, method, args) -> null);

    @SuppressWarnings("unchecked")
    private static <X> X proxy(final Class<X> type, final java.lang.reflect.InvocationHandler handler) {
        return (X) Proxy.newProxyInstance(AssociatedListTestCase.class.getClassLoader(), new Class<?>[]{type}, handler);
    }

    /** A list whose DAO returns the given entities, an unmodifiable list like the real DAOs, counting the loads. */
    @SuppressWarnings("unchecked")
    private AssociatedList<OrmEntity, Uplink> newList(final AtomicInteger loads, final Supplier<List<OrmEntity>> loaded) {
        final Dao<OrmEntity> dao = proxy(Dao.class, (proxy, method, args) -> {
            if (method.getName().equals("lazyLoadByAssociation")) {
                loads.incrementAndGet();
                Thread.sleep(50); // long enough for the other threads to arrive while this one loads
                return loaded.get();
            }
            return null;
        });
        final EntityWrangler<OrmEntity, Uplink> wrangler = proxy(EntityWrangler.class,
                (proxy, method, args) -> method.getName().equals("retrieveDao") ? dao : null);
        return new AssociatedList<>(null, OrmEntity.class, "parent_id", wrangler, () -> null, (a, b) -> 0);
    }

    /**
     * A parent entity in a cache is shared by all threads; when several of them load its associated entities at once, they must be loaded
     * once, not once per thread.
     */
    @Test
    void concurrentLoadsLoadOnce() throws Exception {
        final AtomicInteger loads = new AtomicInteger();
        final AssociatedList<OrmEntity, Uplink> associated = newList(loads, () -> List.of(record, record));

        final int threads = 8;
        final CountDownLatch start = new CountDownLatch(1);
        final List<Thread> started = new ArrayList<>();
        final List<Throwable> failures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final Thread thread = new Thread(() -> {
                try {
                    start.await();
                    associated.lazyLoad();
                    assertEquals(2, associated.stream().count());
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
        assertEquals(2, associated.size());
        assertTrue(associated.isLoaded());
    }

    /** The DAO returns an unmodifiable list, which a sorter must not try to sort in place. */
    @Test
    void sortsAnUnmodifiableResult() {
        final AssociatedList<OrmEntity, Uplink> associated = newList(new AtomicInteger(), () -> List.of(record, record));
        associated.lazyLoad();
        assertEquals(2, associated.size());
    }

}
