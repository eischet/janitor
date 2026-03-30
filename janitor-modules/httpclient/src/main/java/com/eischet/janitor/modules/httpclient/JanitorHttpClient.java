package com.eischet.janitor.modules.httpclient;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.json.impl.GsonOutputStream;
import com.eischet.janitor.json.impl.JsonExportControls;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportable;
import com.eischet.janitor.toolbox.json.api.JsonOutputSupport;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import com.eischet.janitor.toolbox.memory.RefCounter;
import com.eischet.janitor.toolbox.strings.StringHelpers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Simple HTTP Client for the Janitor scripting language.
 */
public class JanitorHttpClient extends JanitorComposed<JanitorHttpClient> implements JanitorCleanupRequired {

    private static final DispatchTable<JanitorHttpClient> DISPATCH = new DispatchTable<>();
    private static final Logger log = LoggerFactory.getLogger(JanitorHttpClient.class);
    private static final String authHeaderName = "Authorization";

    static {
        DISPATCH.addBuilderMethod("proxy", (self, process, args) -> self.setProxy(args.getString(0).janitorGetHostValue(), args.getInt(1).getAsInt()));
        DISPATCH.addBuilderMethod("allowCookies", (self, process, args) -> self.allowCookies());
        DISPATCH.addBuilderMethod("basic", (self, process, args) -> self.basic(args.getRequiredStringValue(0), args.getRequiredStringValue(1)));
        DISPATCH.addBuilderMethod("verbose", (self, process, args) -> self.verbose());
        DISPATCH.addBuilderMethod("setTimeout", (self, process, args) -> self.setConnectTimeoutSeconds(args.getInt(0).getAsInt()));
        DISPATCH.addBuilderMethod("setRequestTimeout", (self, process, args) -> self.requestTimeoutSeconds = args.getInt(0).getAsInt());
        DISPATCH.addBuilderMethod("ignoreSecurityIssues", (self, process, args) -> self.ignoreSecurityIssues(args.get(0).janitorIsTrue()));
        DISPATCH.addBuilderMethod("addHeader", (self, process, args) -> self.addHeader(args.getRequiredStringValue(0), args.getRequiredStringValue(1)));
        DISPATCH.addBuilderMethod("setBearerToken", (self, process, args) -> self.setBearerToken(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setPrettyJson", (self, process, args) -> self.prettyJson = args.get(0).janitorIsTrue());
        DISPATCH.addBuilderMethod("convertJsonToPureAscii", (self, process, args) -> self.convertJsonToPureAscii = args.get(0).janitorIsTrue());
        DISPATCH.addBooleanProperty("prettyJson", self -> self.prettyJson, (self, value) -> self.prettyJson = value);
        DISPATCH.addVoidMethod("close", (self, process, args) -> self.close());

        DISPATCH.addVoidMethod("_test_throwNativeException", (self, process, args) -> {
            log.info("TEST: Throwing a native exception!");
            throw new JanitorNativeException(process, "TEST: Throwing a native exception!", null);
        });

        DISPATCH.addVoidMethod("delete", (self, process, args) -> {
            try {
                self.delete(args.getString(0).janitorGetHostValue());
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });
        DISPATCH.addMethod("getString", (self, process, args) -> {
            try {
                return process.getBuiltins().nullableString(self.getString(args.getRequiredStringValue(0)));
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });
        DISPATCH.addMethod("getBinary", (self, process, args) -> {
            try {
                final byte[] bytes = self.getBinary(args.getRequiredStringValue(0));
                if (bytes == null || bytes.length == 0) {
                    return Janitor.NULL;
                } else {
                    return Janitor.binary(bytes);
                }
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });

        DISPATCH.addMethod("getJson", (self, process, args) -> {
            try {
                return process.getBuiltins().nullableString(self.getJson(args.getRequiredStringValue(0)));
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });
        DISPATCH.addMethod("postForm", (self, process, args) -> {
            try {
                return process.getBuiltins().nullableString(self.postForm(args.getRequiredStringValue(0), convertToStringStringMap(args.get(1))));
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });
        DISPATCH.addMethod("postJson", (self, process, args) -> {
            try {
                return process.getBuiltins().nullableString(self.postJson(args.getRequiredStringValue(0), self.convertToValidJson(process, args.get(1))));
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });
        DISPATCH.addMethod("putJson", (self, process, args) -> {
            try {
                return process.getBuiltins().nullableString(self.putJson(args.getRequiredStringValue(0), self.convertToValidJson(process, args.get(1))));
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });
        DISPATCH.addMethod("patchJson", (self, process, args) -> {
            try {
                return process.getBuiltins().nullableString(self.patchJson(args.getRequiredStringValue(0), self.convertToValidJson(process, args.get(1))));
            } catch (HttpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        });
    }

    private final Map<String, String> additionalHeaders = new HashMap<>();
    private final Set<String> ignoreErrorHeaders = new HashSet<>();
    private String authHeaderContents = null;
    private int connectTimeoutSeconds = 60;
    private int requestTimeoutSeconds = 300;
    private CopyOnWriteArrayList<String> cookies = null;
    private boolean verbose = false;
    private boolean prettyJson = false;
    private boolean insecure;
    private InetSocketAddress proxy;
    private boolean convertJsonToPureAscii = false;
    private boolean mustRebuild = false;
    private HttpClient builtClient = null;
    private final RefCounter refCounter = new RefCounter(this::cleanClose);

    public JanitorHttpClient() {
        super(DISPATCH);
    }

    public static JanitorHttpClient create() {
        return new JanitorHttpClient();
    }

    private static Map<String, String> convertToStringStringMap(final JanitorObject janitorObject) {
        final Map<String, String> result = new HashMap<>();
        if (janitorObject instanceof JMap map) {
            for (final JanitorObject key : map.keySet()) {
                final JanitorObject value = map.get(key);
                result.put(key.janitorToString(), value.janitorToString());
            }
        } else {
            throw new IllegalArgumentException("Invalid argument, expected Map: " + janitorObject);
        }
        return result;
    }

    public JanitorHttpClient addHeader(final String header, final String value) {
        additionalHeaders.put(header, value);
        return this;
    }

    public void setConnectTimeoutSeconds(final int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
        this.mustRebuild = true;
    }

    public JanitorHttpClient allowCookies() {
        cookies = new CopyOnWriteArrayList<>();
        return this;
    }

    public JanitorHttpClient basic(final String username, final String password) {
        if (username == null || username.isEmpty()) {
            authHeaderContents = null;
        } else {
            authHeaderContents = "Basic " + Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        }
        return this;
    }

    public JanitorHttpClient ignoreSecurityIssues(final boolean yes) {
        insecure = yes;
        mustRebuild = true;
        return this;
    }

    private HttpRequest configureRequest(final HttpRequest.Builder req) {
        if (authHeaderContents != null) {
            req.header(authHeaderName, authHeaderContents);
        }
        if (requestTimeoutSeconds > 0) {
            req.timeout(Duration.of(requestTimeoutSeconds, ChronoUnit.SECONDS));
        }
        additionalHeaders.forEach(req::header);
        if (cookies != null) {
            for (final String cookie : cookies) {
                req.header("Cookie", cookie);
            }
        }
        return req.build();
    }

    private HttpClient buildClient() {
        if (builtClient == null || mustRebuild) {
            builtClient = _buildClient();
            mustRebuild = false;
        }
        return builtClient;
    }

    private HttpClient _buildClient() {
        try {
            if (insecure) {
                // Ich weiß nicht, was für ein Problem die Penner vom JDK haben, dass dieses Problem auch nach 100 Jahren noch besteht:
                // https://stackoverflow.com/questions/52988677/allow-insecure-https-connection-for-java-jdk-11-httpclient
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
                return HttpClient.newBuilder().sslContext(BlindSSLSocketFactory.getSSLContext())
                        .connectTimeout(Duration.of(connectTimeoutSeconds, ChronoUnit.SECONDS))
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();
            }
        } catch (Exception e) {
            log.error("FAILED to create an insecure HTTP client!");
        }
        final HttpClient.Builder builder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.of(connectTimeoutSeconds, ChronoUnit.SECONDS));
        if (proxy != null) {
            builder.proxy(ProxySelector.of(proxy));
        }
        return builder.build();
    }

    public JanitorHttpClient setProxy(final InetSocketAddress proxy) {
        // Authentication für den Proxy muss man separat liefern: https://stackoverflow.com/questions/53333556/proxy-authentication-with-jdk-11-httpclient
        // Als Header "Proxy-Authorization".
        this.proxy = proxy;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public JanitorHttpClient setProxy(final String host, final int port) {
        setProxy(InetSocketAddress.createUnresolved(host, port));
        return this;
    }

    private void report(HttpRequest req, final String body) {
        if (verbose) {
            if (log.isInfoEnabled()) {
                log.info("HTTP {} {}, headers {}, body {}", req.method(), req.uri(), req.headers(), StringHelpers.cut(body, 4000));
            }
        }
    }

    private void report(HttpResponse<?> resp) {
        if (verbose) {
            log.info("HTTP response: status {}, body {}, headers {}", resp.statusCode(), resp.body(), resp.headers());
        }
    }

    public void delete(final String url) throws HttpException {
        final HttpRequest req = configureRequest(HttpRequest.newBuilder(URI.create(url)).DELETE());
        var future = buildClient().sendAsync(req, HttpResponse.BodyHandlers.discarding());
        try {
            future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
        } catch (RuntimeException e) {
            future.cancel(true);
            throw HttpException.failedDelete(url);
        }
    }

    public byte[] getBinary(final String url) throws HttpException {
        final HttpRequest req = configureRequest(HttpRequest.newBuilder(URI.create(url)).GET());
        report(req, null);
        final CompletableFuture<HttpResponse<byte[]>> future = buildClient().sendAsync(req, HttpResponse.BodyHandlers.ofByteArray());
        try {
            final HttpResponse<byte[]> resp = future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
            report(resp);
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp.body();
            } else {
                throw new HttpException(null, resp.statusCode(), "GET", url, cleanHeaders(resp.headers().map()), null, null);
            }
        } catch (RuntimeException e) {
            future.cancel(true);
            throw new HttpException("GET", url, e);
        }
    }

    public String getString(final String url) throws HttpException {
        final HttpRequest req = configureRequest(HttpRequest.newBuilder(URI.create(url)).GET());
        report(req, null);
        final var client = buildClient();
        var future = client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        try {
            final HttpResponse<String> resp = future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
            report(resp);
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp.body();
            } else {
                throw new HttpException(null, resp.statusCode(), "GET", url, cleanHeaders(resp.headers().map()), null, null);
            }
        } catch (RuntimeException e) {
            future.cancel(true);
            throw new HttpException("GET", url, e);
        }
    }

    /**
     * Remove unwanted headers, join lists.
     * This method is only useful for displaying headers, not for sending them!
     *
     * @param map a map of headers
     * @return a cleaned version of the map
     */
    private Map<String, String> cleanHeaders(final Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>();
        map.forEach((key, value) -> {
            if (!ignoreErrorHeaders.contains(key.toLowerCase(Locale.ROOT))) {
                if (value.size() == 1) {
                    result.put(key, value.getFirst());
                } else {
                    result.put(key, value.toString());
                }
            }
        });
        return result;
    }

    private Map<String, String> cleanHeadersPlain(final Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> result = new HashMap<>();
        map.forEach((key, value) -> {
            if (!ignoreErrorHeaders.contains(key.toLowerCase(Locale.ROOT))) {
                result.put(key, value);
            }
        });
        return result;
    }

    public String getJson(final String url) throws HttpException {
        final HttpRequest req = configureRequest(HttpRequest.newBuilder(URI.create(url)).GET().header("Accept", "application/json; charset=utf-8"));
        report(req, null);
        final var client = buildClient();
        if (verbose) {
            log.info("GET {} with connect timeout {}s, request timeout {}s", url, connectTimeoutSeconds, requestTimeoutSeconds);
        }
        var future = client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        try {
            final HttpResponse<String> httpResponse = future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
            report(httpResponse);
            final JanitorJsonResponse resp = new JanitorJsonResponse(httpResponse, url, "GET");
            resp.throwIfFailed(resp);
            return resp.getBody();
        } catch (IOException | RuntimeException e) {
            future.cancel(true);
            throw new HttpException("GET", url, e);
        }
    }

    public String postForm(final String url, final Map<String, String> values) throws HttpException {
        final String body = encodeForm(values);
        final HttpRequest req = configureRequest(
                HttpRequest.newBuilder(
                                URI.create(url))
                        .header("content-type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body.getBytes(StandardCharsets.UTF_8)))

        );
        report(req, body);
        final HttpClient client = buildClient();
        final var future = client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        try {
            final HttpResponse<String> httpResponse = future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
            report(httpResponse);
            final JanitorJsonResponse resp = new JanitorJsonResponse(httpResponse, url, "POST");
            resp.throwIfFailed(resp);
            return resp.getBody();
        } catch (RuntimeException | IOException e) {
            future.cancel(true);
            throw HttpException.failedPost(url, values.toString(), e);
        }
    }

    private String encodeForm(final Map<String, String> values) {
        final StringBuilder sb = new StringBuilder();
        values.forEach((key, value) -> {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        });
        return sb.toString();
    }

    public String postJson(final String url, final String json) throws HttpException {
        final HttpRequest req = configureRequest(
                HttpRequest.newBuilder(
                                URI.create(url)).POST(HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json; charset=utf-8")

        );
        report(req, json);
        final var client = buildClient();
        final var future = client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        try {
            final HttpResponse<String> httpResponse = future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
            report(httpResponse);
            final JanitorJsonResponse resp = new JanitorJsonResponse(httpResponse, url, "POST");
            resp.throwIfFailed(resp);
            return resp.getBody();
        } catch (RuntimeException | IOException e) {
            try {
                future.cancel(true);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            throw HttpException.failedPost(url, json, e);
        }
    }

    public String putJson(final String url, final String json) throws HttpException {
        final HttpRequest req = configureRequest(
                HttpRequest.newBuilder(URI.create(url)).PUT(HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json; charset=utf-8")

        );
        report(req, json);
        final var client = buildClient();
        final var future = client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        try {
            final HttpResponse<String> httpResponse = future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
            report(httpResponse);
            final JanitorJsonResponse resp = new JanitorJsonResponse(httpResponse, url, "PUT");
            resp.throwIfFailed(resp);
            return resp.getBody();
        } catch (RuntimeException | IOException e) {
            future.cancel(true);
            throw HttpException.failedPost(url, json, e);
        }
    }

    public String patchJson(final String url, final String json) throws HttpException {
        final HttpRequest req = configureRequest(
                HttpRequest.newBuilder(
                                URI.create(url)).method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json; charset=utf-8")

        );
        report(req, json);
        final var client = buildClient();
        final var future = client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
        try {
            final HttpResponse<String> httpResponse = future.orTimeout(requestTimeoutSeconds, TimeUnit.SECONDS).join();
            report(httpResponse);
            final JanitorJsonResponse resp = new JanitorJsonResponse(httpResponse, url, "POST");
            resp.throwIfFailed(resp);
            return resp.getBody();

        } catch (RuntimeException | IOException e) {
            future.cancel(true);
            throw HttpException.failedPost(url, json, e);
        }
    }

    @Override
    public JanitorHttpClient janitorGetHostValue() {
        return this;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "http-client";
    }

    public JanitorHttpClient verbose() {
        this.verbose = true;
        return this;
    }

    public void ignoreErrorHeaders(final Set<String> ignoreResponseHeaders) {
        ignoreResponseHeaders.forEach(header -> ignoreErrorHeaders.add(header.toLowerCase(Locale.ROOT)));
    }

    /* dieser Code könnte noch interessant werden, aus altem CockpitHttpClient. Momentan (August '21) gibt es keinen Weg, dem
       neuen Http-Client einen Hostname Verifier mitzugeben... das haben sie wohl einfach vergessen.
        https://bugs.openjdk.java.net/browse/JDK-8213309

    private static CloseableHttpClient createAcceptSelfSignedCertificateClient() throws Exception {

        // via https://memorynotfound.com/ignore-certificate-errors-apache-httpclient/

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        //SSLContext sslContext = SSLContextBuilder
        //    .create()
        //    .loadTrustMaterial(new TrustSelfSignedStrategy())
        //    .build();

        SSLContext sslContext = BlindSSLSocketFactory.getSSLContext();

        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(connectionFactory)
            .build();

        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients
            .custom()
            .setConnectionManager(cm)
            .build();
    }
    */

    public JanitorHttpClient setBearerToken(final String token) {
        return addHeader("Authorization", "Bearer " + token);
    }

    public String convertToValidJson(final JanitorScriptProcess proc, final JanitorObject arg) throws JanitorArgumentException {
        final String json = _convertToValidJson(proc, arg);
        if (json != null && convertJsonToPureAscii) {
            return json.chars()
                    .mapToObj(c -> c < 128 ? String.valueOf((char) c) : String.format("\\u%04x", c))
                    .collect(Collectors.joining());
        }
        return json;
    }

    public String _convertToValidJson(final JanitorScriptProcess proc, final JanitorObject arg) throws JanitorArgumentException {
        if (arg instanceof JString str) {
            return str.janitorGetHostValue();
        }
        if (arg instanceof JsonExportable exportable) {
            try {
                if (prettyJson) {
                    return exportable.exportToJson(new PrettyEnvWrapper(proc.getRuntime().getEnvironment()));
                } else {
                    return exportable.exportToJson(proc.getRuntime().getEnvironment());
                }
            } catch (JsonException e) {
                throw new JanitorArgumentException(proc, "failed to convert to JSON: " + arg + " [" + simpleClassNameOf(arg) + "]", e);
            }
        }
        if (arg instanceof JsonWriter jw) {
            try {
                if (prettyJson) {
                    final GsonOutputStream.GsonStringOut out = new GsonOutputStream.GsonStringOut(JsonExportControls.pretty());
                    jw.writeJson(out);
                    return out.getString();
                } else {
                    final GsonOutputStream.GsonStringOut out = new GsonOutputStream.GsonStringOut(JsonExportControls.standard());
                    jw.writeJson(out);
                    return out.getString();
                }
            } catch (JsonException e) {
                throw new JanitorArgumentException(proc, "failed to convert to JSON: " + arg + " [" + simpleClassNameOf(arg) + "]", e);
            }
        }
        throw new JanitorArgumentException(proc, "failed to convert to JSON: " + arg + " [" + simpleClassNameOf(arg) + "]");
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(final int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public void cleanClose() {
        if (verbose) {
            log.info("closing HTTP client (auto closed via ref counter)");
        }
        close();
    }

    public void close() {
        if (builtClient != null && !builtClient.isTerminated()) {
            if (verbose) {
                log.info("closing HTTP client");
            }
            // client.close can go into a loop for one full day (!) when called, so we work around that by spawning a shutdown thread.
            final Thread cleanupThread = new Thread() {
                @Override
                public void run() {
                    builtClient.shutdownNow();
                }
            };
            cleanupThread.setDaemon(true);
            cleanupThread.start();
        }
    }

    @Override
    public void janitorEnterScope() {
        if (verbose) {
            log.info("entering scope");
        }
        try {
            refCounter.acquire();
        } catch (IllegalStateException e) {
            log.warn("ref counter error {}", e.getMessage());
        }
    }

    @Override
    public void janitorLeaveScope() {
        if (verbose) {
            log.info("leaving scope");
        }
        try {
            refCounter.release();
        } catch (IllegalStateException e) {
            log.warn("ref counter error", e);
        }
    }

    @Override
    public void janitorCleanup() {
        if (builtClient != null && !builtClient.isTerminated()) {
            log.info("auto-closing HTTP client");
            builtClient.close();
        }
    }

    private static class PrettyEnvWrapper implements JsonOutputSupport {
        public PrettyEnvWrapper(final JanitorEnvironment environment) {
        }

        @Override
        public String writeJson(final JsonWriter writer) throws JsonException {
            final GsonOutputStream.GsonStringOut out = new GsonOutputStream.GsonStringOut(JsonExportControls.pretty());
            writer.writeJson(out);
            return out.getString();
        }

    }

    public class JanitorJsonResponse {
        private final String url;
        private final String method;
        private final int code;
        private final Map<String, String> headers;
        private final String body;

        public JanitorJsonResponse(final HttpResponse<String> response, final String url, final String method) throws IOException {
            this.url = url;
            this.method = method;
            code = response.statusCode();
            headers = new HashMap<>();

            for (final var header : response.headers().map().entrySet()) {
                for (final String s : header.getValue()) {
                    if (cookies != null && Objects.equals("Set-Cookie", header.getKey())) {
                        cookies.add(s);
                    }
                    headers.put(header.getKey(), s);
                }
            }
            body = response.body();
        }

        public int getCode() {
            return code;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }

        public void throwIfFailed(final JanitorJsonResponse resp) throws HttpException {
            if (code < 200 || code >= 400) {
                final String respBody = resp.getBody();
                throw new HttpException(null, code, method, url, cleanHeadersPlain(headers), respBody, null);
            }
        }
    }
}

