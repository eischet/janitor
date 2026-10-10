package com.eischet.janitor.modules.httpclient;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.JanitorException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorAware;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/** Thrown when an HTTP request fails, either because of the response status or because of a network error. */
public class HttpException extends JanitorException implements JanitorAware {
    private final Map<String, String> headers;
    private final String body;
    private final String method;
    private final String url;
    private final int code;

    public HttpException(final @Nullable String request,
                         final int code,
                         final @NotNull String method,
                         final @NotNull String url,
                         final @Nullable Map<String, String> headers,
                         final @Nullable String body,
                         final @Nullable Throwable cause) {
        super(produceMessage(request, code, method, url, body), cause);
        this.code = code;
        this.method = method;
        this.url = url;
        this.headers = headers == null ? Collections.emptyMap() : Map.copyOf(headers);
        this.body = body;
    }

    public HttpException(final String method, final String url, final Throwable cause) {
        this(null, 0, method, url, null, null, cause);
    }

    /**
     * @param url the URL
     * @return an exception for a failed DELETE request
     */
    public static HttpException failedDelete(final String url) {
        return new HttpException(null, 0, "DELETE", url, null, null, null);
    }

    /**
     * @param url the URL
     * @param request a description of the request
     * @param cause the reason of the failure
     * @return an exception for a failed POST request
     */
    public static HttpException failedPost(final String url, final String request, final Throwable cause) {
        return new HttpException(request, 0, "POST", url, null, null, cause);
    }

    /**
     * @param url the URL
     * @param request a description of the request
     * @param cause the reason of the failure
     * @return an exception for a failed GET request
     */
    public static HttpException failedGet(final String url, final String request, final Throwable cause) {
        return new HttpException(request, 0, "GET", url, null, null, cause);
    }

    /**
     * @param url the URL
     * @param request a description of the request
     * @param cause the reason of the failure
     * @return an exception for a failed PUT request
     */
    public static HttpException failedPut(final String url, final String request, final Throwable cause) {
        return new HttpException(request, 0, "PUT", url, null, null, cause);
    }

    /**
     * @param url the URL
     * @param request a description of the request
     * @param cause the reason of the failure
     * @return an exception for a failed PATCH request
     */
    public static HttpException failedPatch(final String url, final String request, final Throwable cause) {
        return new HttpException(request, 0, "PATCH", url, null, null, cause);
    }


    private static String produceMessage(final @Nullable String request, final int code, final @NotNull String method, final @NotNull String url, final @Nullable String responseBody) {
        final StringBuilder out = new StringBuilder();
        if (code > 0) {
            out.append("Error ").append(code).append(" in ");
        } else {
            out.append("Exception in ");
        }
        out.append(method).append(" ").append(url);
        if (request != null) {
            out.append("; our request was: ").append(request);
        }
        if (responseBody != null) {
            out.append("; server response: ").append(responseBody);
        }
        return out.toString();
    }


    /**
     * @return the body of the response, if any
     */
    public String getBody() {
        return body;
    }

    /**
     * @return the headers of the response
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @return the HTTP method of the request
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the URL of the request
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the HTTP status code, or 0 if there was no response
     */
    public int getCode() {
        return code;
    }

    @Override
    public JanitorObject asJanitorObject() {
        return new Wrapper(this);
    }

    private static class Wrapper extends JanitorWrapper<HttpException> {

        private static final WrapperDispatchTable<HttpException> DISPATCHER = new WrapperDispatchTable<>();

        static {
            DISPATCHER.addStringProperty("url", self -> self.janitorGetHostValue().getUrl());
            DISPATCHER.addStringProperty("method", self -> self.janitorGetHostValue().getMethod());
            DISPATCHER.addStringProperty("body", self -> self.janitorGetHostValue().getBody());
            DISPATCHER.addIntegerProperty("code", self -> self.janitorGetHostValue().getCode());
            DISPATCHER.addObjectProperty("headers", self -> {
                final @NotNull JMap map = Janitor.map();
                self.janitorGetHostValue().getHeaders().forEach(map::put);
                return map;
            });
            // DISPATCHER.addMapProperty("headers", self -> self.janitorGetHostValue().getHeaders(), null);

        }


        public Wrapper(final HttpException httpException) {
            super(DISPATCHER, httpException);
        }
    }
}