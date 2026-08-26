package com.eischet.janitor.modules.httpclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression tests for JanitorHttpClient's close()/janitorCleanup() self-healing behavior.
 * <p>
 * close() (and janitorCleanup(), and janitorLeaveScope()'s ref-counter callback) used to shut the
 * underlying java.net.http.HttpClient down without ever resetting the builtClient/mustRebuild
 * fields, so buildClient()'s "rebuild if needed" check ({@code builtClient == null || mustRebuild})
 * never triggered afterward -- any use after a close (intentional, or premature due to the
 * ScopeEnterLeaveBalanceTestCase-class of bug, or a shared, cached module-scope client being closed
 * while other scripts still hold a reference to it) was left pointing at a dead client forever.
 * <p>
 * builtClient is a private field, and this sandboxed test environment cannot construct a real
 * {@code java.net.http.HttpClient} at all (its internal loopback Selector/Pipe setup fails here with
 * "Unable to establish loopback connection" / SocketException, unrelated to this fix) -- so these
 * tests inject a minimal fake HttpClient via reflection instead of exercising buildClient()'s actual
 * client construction. That means the "does a rebuilt client actually work" question isn't covered
 * here (nor was it changed by this fix: buildClient()'s own rebuild condition was already correct --
 * the bug was purely that builtClient/mustRebuild were never reset to make it trigger), only "does
 * close()/janitorCleanup()/janitorLeaveScope() correctly reset builtClient to null and shut down the
 * previously-held client exactly once, including when raced from multiple threads".
 */
public class JanitorHttpClientCloseTestCase {

    /**
     * Minimal HttpClient stand-in: only isTerminated()/shutdownNow() have real behavior (that's all
     * close() touches); every other abstract method throws, since none of these tests call them.
     */
    static class FakeHttpClient extends HttpClient {
        final AtomicInteger shutdownNowCalls = new AtomicInteger();
        volatile boolean terminated = false;

        @Override
        public void shutdownNow() {
            shutdownNowCalls.incrementAndGet();
            terminated = true;
        }

        @Override
        public boolean isTerminated() {
            return terminated;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Redirect followRedirects() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ProxySelector> proxy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SSLContext sslContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SSLParameters sslParameters() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Version version() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Executor> executor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> HttpResponse<T> send(final HttpRequest request, final HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request, final HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request, final HttpResponse.BodyHandler<T> responseBodyHandler, final HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WebSocket.Builder newWebSocketBuilder() {
            throw new UnsupportedOperationException();
        }
    }

    private static void setBuiltClientField(final JanitorHttpClient client, final HttpClient value) throws Exception {
        final Field f = JanitorHttpClient.class.getDeclaredField("builtClient");
        f.setAccessible(true);
        f.set(client, value);
    }

    private static HttpClient getBuiltClientField(final JanitorHttpClient client) throws Exception {
        final Field f = JanitorHttpClient.class.getDeclaredField("builtClient");
        f.setAccessible(true);
        return (HttpClient) f.get(client);
    }

    /**
     * close() intentionally shuts the old client down asynchronously, on a fire-and-forget daemon
     * thread (that's the whole point of the fix for the "client.close() can hang for a day" issue),
     * so shutdownNowCalls only becomes observable a short, unspecified time after close() returns --
     * poll for it instead of asserting immediately, to avoid a flaky race in the test itself.
     */
    private static void awaitShutdownCalls(final FakeHttpClient fake, final int expected) throws InterruptedException {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (fake.shutdownNowCalls.get() != expected && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
    }

    @Test
    public void closeResetsBuiltClientAndShutsDownTheOldOne() throws Exception {
        final JanitorHttpClient client = JanitorHttpClient.create();
        final FakeHttpClient fake = new FakeHttpClient();
        setBuiltClientField(client, fake);

        client.close();

        assertNull(getBuiltClientField(client), "close() must reset builtClient to null so buildClient() rebuilds instead of reusing a dead client");
        awaitShutdownCalls(fake, 1);
        assertEquals(1, fake.shutdownNowCalls.get(), "the previously held client must actually be shut down");
    }

    @Test
    public void closeOnAFreshlyCreatedClientIsANoOp() {
        final JanitorHttpClient client = JanitorHttpClient.create();
        // no client was ever built -- close() must not throw (e.g. NPE) here.
        client.close();
    }

    @Test
    public void closingTwiceInARowOnlyShutsDownOnce() throws Exception {
        final JanitorHttpClient client = JanitorHttpClient.create();
        final FakeHttpClient fake = new FakeHttpClient();
        setBuiltClientField(client, fake);

        client.close();
        client.close(); // must be a harmless no-op the second time, not throw or shut down again
        assertNull(getBuiltClientField(client));
        awaitShutdownCalls(fake, 1);
        assertEquals(1, fake.shutdownNowCalls.get());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void janitorCleanupDelegatesToTheNonBlockingCloseAndResetsState() throws Exception {
        // Regression guard for the "janitorCleanup() reintroduces the hang close() was meant to
        // avoid" bug: janitorCleanup() used to call builtClient.close() directly, which can block
        // for a very long time. It now delegates to close(), which is non-blocking (spawns a daemon
        // shutdown thread) -- the @Timeout above ensures this test itself would fail loudly instead
        // of hanging forever if that regressed.
        final JanitorHttpClient client = JanitorHttpClient.create();
        final FakeHttpClient fake = new FakeHttpClient();
        setBuiltClientField(client, fake);

        client.janitorCleanup();

        assertNull(getBuiltClientField(client), "janitorCleanup() must also reset builtClient (self-healing), not just shut it down");
        awaitShutdownCalls(fake, 1);
        assertEquals(1, fake.shutdownNowCalls.get());
    }

    @Test
    public void janitorLeaveScopeTriggeredCloseIsAlsoSelfHealing() throws Exception {
        // Mirrors how JanitorHttpClient is actually closed via the ref-counter callback when a
        // script's/module's scope is left (see cleanClose() -> close()).
        final JanitorHttpClient client = JanitorHttpClient.create();
        final FakeHttpClient fake = new FakeHttpClient();
        setBuiltClientField(client, fake);

        client.janitorEnterScope();
        client.janitorLeaveScope(); // ref counter 1 -> 0 -> onZero -> cleanClose() -> close()

        assertNull(getBuiltClientField(client));
        awaitShutdownCalls(fake, 1);
        assertEquals(1, fake.shutdownNowCalls.get());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void concurrentCloseFromManyThreadsShutsDownExactlyOnce() throws Exception {
        // Light smoke test for the close() synchronization: hammer close() from several threads on
        // the same, shared instance (the scenario that's realistic for a JanitorHttpClient bound at
        // a shared/cached module scope, used by several concurrently running scripts) and make sure
        // nothing throws, and -- crucially -- that the underlying client only ever gets shut down
        // once, not once per racing thread.
        final JanitorHttpClient client = JanitorHttpClient.create();
        final FakeHttpClient fake = new FakeHttpClient();
        setBuiltClientField(client, fake);

        final int threads = 16;
        final Thread[] workers = new Thread[threads];
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        client.close();
                    }
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            });
        }
        for (final Thread t : workers) {
            t.start();
        }
        for (final Thread t : workers) {
            t.join();
        }
        if (failure.get() != null) {
            throw new AssertionError("concurrent close() threw", failure.get());
        }
        assertNull(getBuiltClientField(client));
        awaitShutdownCalls(fake, 1);
        assertEquals(1, fake.shutdownNowCalls.get(), "the client must be shut down exactly once, no matter how many threads raced to close() it");
    }

}
