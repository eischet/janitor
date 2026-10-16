// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.ProxyOptions;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.TokenCachePersistenceOptions;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A small Microsoft Graph client for mail folders and messages of a mailbox, using application permissions
 * (client credentials). It talks to the REST API via {@link HttpClient} and maps the JSON responses onto Janitor
 * objects, so the results can be used from Java and from scripts alike.
 * <p>
 * All methods take the mailbox owner ({@code user}: user principal name, mail address or object id) as their first
 * argument, because an application usually works with several mailboxes. Folder ids may also be Graph's well-known
 * names, such as "inbox" or "deleteditems".
 * </p>
 */
public class MsGraphClient extends JanitorComposed<MsGraphClient> {

    private static final Logger log = LoggerFactory.getLogger(MsGraphClient.class);

    private static final String DEFAULT_SCOPE = "https://graph.microsoft.com/.default";
    private static final List<String> FOLDER_SELECT = List.of("id", "displayName", "parentFolderId", "childFolderCount", "totalItemCount", "unreadItemCount");
    private static final int MAX_RETRY_WAIT_SECONDS = 120;

    /**
     * Clients created via {@link #forClientSecret}, so that repeated connects with the same configuration share one
     * credential (and therefore its in-memory token cache) instead of fetching a new token every time.
     */
    private static final Map<ClientKey, MsGraphClient> CLIENTS = new ConcurrentHashMap<>();

    public static final DispatchTable<MsGraphClient> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addMethod("folder", (self, process, args) -> {
            args.require(2);
            final String user = args.getRequiredStringValue(0);
            final String folderId = args.getRequiredStringValue(1);
            return call(process, () -> self.getFolder(user, folderId));
        });
        DISPATCH.addMethod("folders", (self, process, args) -> {
            args.require(1, 2);
            final String user = args.getRequiredStringValue(0);
            final String parentId = args.getOptionalStringValue(1, null);
            return Janitor.list(call(process, () -> self.listFolders(user, parentId)));
        });
        DISPATCH.addMethod("allFolders", (self, process, args) -> {
            args.require(1);
            final String user = args.getRequiredStringValue(0);
            return Janitor.list(call(process, () -> self.listAllFolders(user)));
        });
        DISPATCH.addMethod("findFolder", (self, process, args) -> {
            args.require(2, 3);
            final String user = args.getRequiredStringValue(0);
            final String displayName = args.getRequiredStringValue(1);
            final String parentId = args.getOptionalStringValue(2, null);
            final MailFolder folder = call(process, () -> self.findFolder(user, displayName, parentId));
            return folder == null ? Janitor.NULL : folder;
        });
        DISPATCH.addMethod("createFolder", (self, process, args) -> {
            args.require(2, 3);
            final String user = args.getRequiredStringValue(0);
            final String displayName = args.getRequiredStringValue(1);
            final String parentId = args.getOptionalStringValue(2, null);
            return call(process, () -> self.createFolder(user, displayName, parentId));
        });
        DISPATCH.addVoidMethod("deleteFolder", (self, process, args) -> {
            args.require(2);
            final String user = args.getRequiredStringValue(0);
            final String folderId = args.getRequiredStringValue(1);
            call(process, () -> {
                self.deleteFolder(user, folderId);
                return null;
            });
        });
        DISPATCH.addMethod("messages", (self, process, args) -> {
            args.require(1, 3);
            final String user = args.getRequiredStringValue(0);
            final String folderId = args.getOptionalStringValue(1, "inbox");
            final MessageQuery query = new MessageQuery();
            if (args.size() > 2) {
                args.getRequired(2, JMap.class).applyTo(process, query);
            }
            return Janitor.list(call(process, () -> self.listMessages(user, folderId, query)));
        });
        DISPATCH.addMethod("mime", (self, process, args) -> {
            args.require(2);
            final String user = args.getRequiredStringValue(0);
            final String messageId = args.getRequiredStringValue(1);
            return Janitor.getBuiltins().string(new String(call(process, () -> self.getMimeContent(user, messageId)), StandardCharsets.UTF_8));
        });
        DISPATCH.addMethod("move", (self, process, args) -> {
            args.require(3);
            final String user = args.getRequiredStringValue(0);
            final String messageId = args.getRequiredStringValue(1);
            final String destinationId = args.getRequiredStringValue(2);
            return Janitor.getBuiltins().nullableString(call(process, () -> self.moveMessage(user, messageId, destinationId)));
        });
        DISPATCH.addVoidMethod("update", (self, process, args) -> {
            args.require(3);
            final String user = args.getRequiredStringValue(0);
            final String messageId = args.getRequiredStringValue(1);
            final MessageUpdate update = new MessageUpdate();
            args.getRequired(2, JMap.class).applyTo(process, update);
            call(process, () -> {
                self.updateMessage(user, messageId, update);
                return null;
            });
        });
    }

    private final TokenCredential credential;
    private final MsGraphOptions options;
    private final HttpClient httpClient;
    private final TokenRequestContext tokenRequest = new TokenRequestContext().addScopes(DEFAULT_SCOPE);

    /**
     * Creates a client for any credential; mostly useful for tests and for hosts that build credentials themselves.
     * Prefer {@link #forClientSecret}, which reuses clients and applies the proxy to the token requests, too.
     * @param credential provides the access tokens
     * @param options connection options
     */
    public MsGraphClient(final @NotNull TokenCredential credential, final @NotNull MsGraphOptions options) {
        super(DISPATCH);
        this.credential = credential;
        this.options = options;
        final HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(options.getTimeoutSeconds()))
                .followRedirects(HttpClient.Redirect.NORMAL);
        if (options.hasProxy()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(options.getProxyHost(), options.getProxyPort())));
        }
        this.httpClient = builder.build();
    }

    /**
     * Returns a client that authenticates with a client secret (application permissions). Clients are cached per
     * configuration, so calling this repeatedly, e.g. once per mail import run, reuses the credential and its tokens.
     * The proxy, if any, is used for the token requests to Entra ID as well as for the Graph requests.
     * @param clientId the application (client) id
     * @param clientSecret the client secret
     * @param tenantId the directory (tenant) id
     * @param options connection options
     * @return a client
     */
    public static MsGraphClient forClientSecret(final @NotNull String clientId,
                                                final @NotNull String clientSecret,
                                                final @NotNull String tenantId,
                                                final @NotNull MsGraphOptions options) {
        final ClientKey key = new ClientKey(clientId, clientSecret, tenantId, options.getBaseUrl(),
                options.hasProxy() ? options.getProxyHost() : null, options.hasProxy() ? options.getProxyPort() : 0,
                options.isTokenCache(), options.getTimeoutSeconds(), options.getMaxRetries());
        return CLIENTS.computeIfAbsent(key, k -> {
            final ClientSecretCredentialBuilder builder = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId);
            if (options.hasProxy()) {
                builder.proxyOptions(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(options.getProxyHost(), options.getProxyPort())));
            }
            if (options.isTokenCache()) {
                builder.tokenCachePersistenceOptions(new TokenCachePersistenceOptions());
            }
            log.info("creating Graph client for tenant {}, client id {}{}{}", tenantId, clientId,
                    options.hasProxy() ? ", proxy " + options.getProxyHost() + ":" + options.getProxyPort() : "",
                    options.isTokenCache() ? ", persistent token cache" : "");
            return new MsGraphClient(builder.build(), options);
        });
    }

    /**
     * Fetches a single folder, including its item counts.
     * @param user mailbox owner
     * @param folderId folder id or well-known name, e.g. "inbox"
     * @return the folder
     */
    public MailFolder getFolder(final @NotNull String user, final @NotNull String folderId) {
        final String json = getString(userUrl(user, "mailFolders", folderId) + query(Map.of("$select", String.join(",", FOLDER_SELECT))), null);
        final MailFolder folder = new MailFolder();
        readJson(json, folder);
        return folder;
    }

    /**
     * Lists the folders on one level, reading all pages.
     * @param user mailbox owner
     * @param parentId the parent folder, or null for the top level
     * @return the folders
     */
    public List<MailFolder> listFolders(final @NotNull String user, final @Nullable String parentId) {
        final String url = (parentId == null ? userUrl(user, "mailFolders") : userUrl(user, "mailFolders", parentId, "childFolders"))
                + query(Map.of("$select", String.join(",", FOLDER_SELECT), "$top", "100"));
        final List<MailFolder> folders = new ArrayList<>();
        forEachItem(url, null, MailFolder::new, folders::add);
        return folders;
    }

    /**
     * Lists all folders of a mailbox, depth first, i.e. every folder is directly followed by its subfolders.
     * @param user mailbox owner
     * @return the folders
     */
    public List<MailFolder> listAllFolders(final @NotNull String user) {
        final List<MailFolder> result = new ArrayList<>();
        addFoldersRecursively(user, null, result);
        return result;
    }

    private void addFoldersRecursively(final String user, final @Nullable String parentId, final List<MailFolder> result) {
        for (final MailFolder folder : listFolders(user, parentId)) {
            result.add(folder);
            final Integer children = folder.getChildFolderCount();
            if (folder.getId() != null && (children == null || children > 0)) {
                addFoldersRecursively(user, folder.getId(), result);
            }
        }
    }

    /**
     * Finds a folder by its display name on one level.
     * @param user mailbox owner
     * @param displayName the folder name, compared by Graph (case-insensitive)
     * @param parentId the parent folder, or null for the top level
     * @return the first matching folder, or null
     */
    public @Nullable MailFolder findFolder(final @NotNull String user, final @NotNull String displayName, final @Nullable String parentId) {
        final String url = (parentId == null ? userUrl(user, "mailFolders") : userUrl(user, "mailFolders", parentId, "childFolders"))
                + query(Map.of("$select", String.join(",", FOLDER_SELECT), "$filter", "displayName eq '" + displayName.replace("'", "''") + "'"));
        final List<MailFolder> folders = new ArrayList<>();
        forEachItem(url, null, MailFolder::new, folder -> {
            folders.add(folder);
            return false;
        });
        return folders.isEmpty() ? null : folders.getFirst();
    }

    /**
     * Creates a (visible) folder.
     * @param user mailbox owner
     * @param displayName the folder name
     * @param parentId the parent folder, or null for the top level
     * @return the new folder
     */
    public MailFolder createFolder(final @NotNull String user, final @NotNull String displayName, final @Nullable String parentId) {
        final String url = parentId == null ? userUrl(user, "mailFolders") : userUrl(user, "mailFolders", parentId, "childFolders");
        final String json = send("POST", url, jsonBody(stream -> stream.beginObject()
                .key("displayName").value(displayName)
                .key("isHidden").value(false)
                .endObject()), null).body();
        final MailFolder folder = new MailFolder();
        readJson(json, folder);
        return folder;
    }

    /**
     * Deletes a folder; Graph moves it to "Deleted Items".
     * @param user mailbox owner
     * @param folderId the folder id
     */
    public void deleteFolder(final @NotNull String user, final @NotNull String folderId) {
        send("DELETE", userUrl(user, "mailFolders", folderId), null, null);
    }

    /**
     * Lists all messages of a folder, reading all pages.
     * @param user mailbox owner
     * @param folderId folder id or well-known name, e.g. "inbox"
     * @param query what to load and in which order
     * @return the messages
     */
    public List<MailMessage> listMessages(final @NotNull String user, final @NotNull String folderId, final @NotNull MessageQuery query) {
        final List<MailMessage> messages = new ArrayList<>();
        forEachMessage(user, folderId, query, message -> {
            messages.add(message);
            return true;
        });
        return messages;
    }

    /**
     * Visits the messages of a folder page by page.
     * @param user mailbox owner
     * @param folderId folder id or well-known name, e.g. "inbox"
     * @param query what to load and in which order
     * @param consumer receives each message; returning false stops reading further messages and pages
     */
    public void forEachMessage(final @NotNull String user, final @NotNull String folderId, final @NotNull MessageQuery query, final @NotNull Predicate<MailMessage> consumer) {
        final Map<String, String> params = new LinkedHashMap<>();
        params.put("$select", String.join(",", query.getSelect()));
        params.put("$top", String.valueOf(query.getTop()));
        if (query.getOrderBy() != null && !query.getOrderBy().isBlank()) {
            params.put("$orderby", query.getOrderBy());
        }
        if (query.getFilter() != null && !query.getFilter().isBlank()) {
            params.put("$filter", query.getFilter());
        }
        final String prefer = query.getBodyType() == null || query.getBodyType().isBlank() ? null : "outlook.body-content-type=\"" + query.getBodyType() + "\"";
        forEachItem(userUrl(user, "mailFolders", folderId, "messages") + query(params), prefer, MailMessage::new, consumer);
    }

    /**
     * Fetches the complete message in MIME format (RFC 822), as stored by Exchange.
     * @param user mailbox owner
     * @param messageId the message id
     * @return the raw MIME content
     */
    public byte[] getMimeContent(final @NotNull String user, final @NotNull String messageId) {
        return execute(request(userUrl(user, "messages", messageId, "$value"), null).GET().build(), HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    /**
     * Moves a message to another folder.
     * @param user mailbox owner
     * @param messageId the message id
     * @param destinationId folder id or well-known name, e.g. "deleteditems"
     * @return the id of the moved message, which Graph changes when moving
     */
    public @Nullable String moveMessage(final @NotNull String user, final @NotNull String messageId, final @NotNull String destinationId) {
        final String json = send("POST", userUrl(user, "messages", messageId, "move"),
                jsonBody(stream -> stream.beginObject().key("destinationId").value(destinationId).endObject()), null).body();
        final MailMessage moved = new MailMessage();
        readJson(json, moved);
        return moved.getId();
    }

    /**
     * Changes a message; only the properties set on the update are sent.
     * @param user mailbox owner
     * @param messageId the message id
     * @param update the changes
     */
    public void updateMessage(final @NotNull String user, final @NotNull String messageId, final @NotNull MessageUpdate update) {
        if (update.isEmpty()) {
            return;
        }
        send("PATCH", userUrl(user, "messages", messageId), jsonBody(update::writePatchBody), null);
    }

    /**
     * @return the options this client was created with
     */
    public MsGraphOptions getOptions() {
        return options;
    }

    private <T extends JanitorComposed<T>> void forEachItem(final String firstUrl, final @Nullable String prefer, final Supplier<T> constructor, final Predicate<T> consumer) {
        String url = firstUrl;
        while (url != null) {
            final List<T> items = new ArrayList<>();
            final String next = readPage(getString(url, prefer), constructor, items);
            for (final T item : items) {
                if (!consumer.test(item)) {
                    return;
                }
            }
            url = next;
        }
    }

    /**
     * Reads one page of a Graph collection: the items from "value", and the link to the next page, if any.
     */
    private static <T extends JanitorComposed<T>> @Nullable String readPage(final String json, final Supplier<T> constructor, final List<T> items) {
        final String[] nextLink = {null};
        readJson(json, stream -> {
            stream.beginObject();
            while (stream.hasNext()) {
                final String key = stream.nextKey();
                if ("value".equals(key)) {
                    stream.beginArray();
                    while (stream.hasNext()) {
                        final T item = constructor.get();
                        item.readJson(stream);
                        items.add(item);
                    }
                    stream.endArray();
                } else if ("@odata.nextLink".equals(key) && stream.peek() == JsonTokenType.STRING) {
                    nextLink[0] = stream.nextString();
                } else {
                    stream.skipValue();
                }
            }
            stream.endObject();
        });
        return nextLink[0];
    }

    private String getString(final String url, final @Nullable String prefer) {
        return execute(request(url, prefer).GET().build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    private HttpResponse<String> send(final String method, final String url, final @Nullable String jsonBody, final @Nullable String prefer) {
        final HttpRequest.Builder builder = request(url, prefer);
        if (jsonBody == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
            log.trace("{} {} body: {}", method, url, jsonBody);
        }
        return execute(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(final String url, final @Nullable String prefer) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(options.getTimeoutSeconds()))
                .header("Accept", "application/json");
        if (prefer != null) {
            builder.header("Prefer", prefer);
        }
        return builder;
    }

    /**
     * Sends a request with a fresh token, retrying throttled (429) and unavailable (503, 504) requests, and turns
     * error responses into {@link MsGraphException}s.
     */
    private <B> HttpResponse<B> execute(final HttpRequest template, final HttpResponse.BodyHandler<B> bodyHandler) {
        int attempt = 0;
        while (true) {
            final HttpRequest request = HttpRequest.newBuilder(template, (name, value) -> true)
                    .setHeader("Authorization", "Bearer " + accessToken())
                    .build();
            final long start = System.nanoTime();
            final HttpResponse<B> response;
            try {
                response = httpClient.send(request, bodyHandler);
            } catch (IOException e) {
                throw new MsGraphException(request.method() + " " + request.uri() + " failed: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MsGraphException(request.method() + " " + request.uri() + " was interrupted", e);
            }
            final int status = response.statusCode();
            log.debug("{} {} -> {} ({} ms)", request.method(), request.uri(), status, (System.nanoTime() - start) / 1_000_000);
            if (status >= 200 && status < 300) {
                return response;
            }
            if ((status == 429 || status == 503 || status == 504) && attempt < options.getMaxRetries()) {
                final int waitSeconds = retryAfterSeconds(response, attempt);
                log.warn("{} {} -> {}, retrying in {} s (attempt {} of {})", request.method(), request.uri(), status, waitSeconds, attempt + 1, options.getMaxRetries());
                sleep(waitSeconds);
                attempt++;
                continue;
            }
            throw toException(request, response);
        }
    }

    private String accessToken() {
        try {
            return Objects.requireNonNull(credential.getTokenSync(tokenRequest), "no token").getToken();
        } catch (RuntimeException e) {
            throw new MsGraphException("cannot get an access token for Microsoft Graph: " + e.getMessage(), e);
        }
    }

    private static int retryAfterSeconds(final HttpResponse<?> response, final int attempt) {
        final String header = response.headers().firstValue("Retry-After").orElse(null);
        if (header != null) {
            try {
                return Math.min(MAX_RETRY_WAIT_SECONDS, Math.max(0, Integer.parseInt(header.trim())));
            } catch (NumberFormatException ignored) {
                // Graph sends seconds; an HTTP date is not expected, so fall back to the exponential wait
            }
        }
        return Math.min(MAX_RETRY_WAIT_SECONDS, 1 << attempt);
    }

    private static void sleep(final int seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MsGraphException("interrupted while waiting to retry a Graph request", e);
        }
    }

    private static MsGraphException toException(final HttpRequest request, final HttpResponse<?> response) {
        final String requestId = response.headers().firstValue("request-id").orElse(null);
        String code = null;
        String message = null;
        if (response.body() instanceof String body && !body.isBlank()) {
            final String[] codeAndMessage = new String[2];
            try {
                readJson(body, stream -> {
                    stream.beginObject();
                    while (stream.hasNext()) {
                        if ("error".equals(stream.nextKey()) && stream.peek() == JsonTokenType.BEGIN_OBJECT) {
                            readErrorObject(stream, codeAndMessage);
                        } else {
                            stream.skipValue();
                        }
                    }
                    stream.endObject();
                });
                code = codeAndMessage[0];
                message = codeAndMessage[1];
            } catch (MsGraphException e) {
                message = body;
            }
        }
        final String text = request.method() + " " + request.uri() + " -> HTTP " + response.statusCode()
                + (code == null ? "" : ", " + code)
                + (message == null ? "" : ": " + message)
                + (requestId == null ? "" : " (request-id " + requestId + ")");
        log.warn(text);
        return new MsGraphException(text, response.statusCode(), code, requestId);
    }

    private static void readErrorObject(final JsonInputStream stream, final String[] codeAndMessage) throws JsonException {
        stream.beginObject();
        while (stream.hasNext()) {
            final String key = stream.nextKey();
            if ("code".equals(key) && stream.peek() == JsonTokenType.STRING) {
                codeAndMessage[0] = stream.nextString();
            } else if ("message".equals(key) && stream.peek() == JsonTokenType.STRING) {
                codeAndMessage[1] = stream.nextString();
            } else {
                stream.skipValue();
            }
        }
        stream.endObject();
    }

    private static void readJson(final String json, final com.eischet.janitor.toolbox.json.api.JsonReader reader) {
        try {
            Janitor.readJson(json, reader);
        } catch (JsonException e) {
            throw new MsGraphException("cannot read Graph response: " + e.getMessage(), e);
        }
    }

    private static String jsonBody(final JsonWriter writer) {
        try {
            return Janitor.writeJson(writer);
        } catch (JsonException e) {
            throw new MsGraphException("cannot write Graph request: " + e.getMessage(), e);
        }
    }

    private String userUrl(final String user, final String... segments) {
        final StringBuilder url = new StringBuilder(options.getBaseUrl()).append("/users/").append(encodePath(user));
        for (final String segment : segments) {
            url.append('/').append("$value".equals(segment) ? segment : encodePath(segment));
        }
        return url.toString();
    }

    private static String encodePath(final String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String query(final Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("&", "?", ""));
    }

    /**
     * Runs a Graph call from a script method, turning Graph errors into script errors.
     */
    private static <R> R call(final JanitorScriptProcess process, final Supplier<R> action) throws JanitorRuntimeException {
        try {
            return action.get();
        } catch (MsGraphException e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "MsGraphClient[" + options.getBaseUrl() + "]";
    }

    private record ClientKey(String clientId, String clientSecret, String tenantId, String baseUrl,
                             @Nullable String proxyHost, int proxyPort, boolean tokenCache, int timeoutSeconds, int maxRetries) {
        @Override
        public String toString() {
            // never let the secret end up in a log
            return "ClientKey[" + tenantId + ", " + clientId + ", " + baseUrl + "]";
        }
    }
}
