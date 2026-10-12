// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest.servlet;

import com.eischet.janitor.orm.rest.OrmRestHandler;
import com.eischet.janitor.orm.rest.RestRequest;
import com.eischet.janitor.orm.rest.RestResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Serves the REST API of an {@link OrmRestHandler} as a servlet. All methods go to the handler, which answers the ones it does not support with
 * {@code 405}. The servlet only translates: the handler decides everything, and the host's {@link com.eischet.janitor.orm.rest.RestPolicy}
 * decides who may do what; who the caller is, the host's {@link RestUserResolver} tells.
 * <p>
 * Map it with a pattern that ends in {@code /*}, e.g. {@code /api/*}: the part after that is the path that the handler routes, so the API
 * description is at {@code /api/openapi.json}.
 * </p>
 * <pre>{@code
 * OrmRestServlet.register(servletContext, "api", "/api/*",
 *         () -> OrmRestHandler.of(daoCollection, myPolicy),
 *         request -> sessionOf(request).user());
 * }</pre>
 */
public class OrmRestServlet<U> extends HttpServlet {

    /** Requests with larger bodies are refused with 413. */
    public static final int DEFAULT_MAX_BODY_CHARS = 1_000_000;

    private final @NotNull Supplier<OrmRestHandler<U>> handlerSupplier;
    private final @NotNull RestUserResolver<? extends U> userResolver;
    private volatile OrmRestHandler<U> handler;
    private int maxBodyChars = DEFAULT_MAX_BODY_CHARS;

    /**
     * @param handlerSupplier   provides the handler when the first request comes, so that the DAOs of the host need not exist when the servlet
     *                          is registered; called once
     * @param userResolver tells who is calling
     */
    public OrmRestServlet(final @NotNull Supplier<OrmRestHandler<U>> handlerSupplier, final @NotNull RestUserResolver<? extends U> userResolver) {
        this.handlerSupplier = handlerSupplier;
        this.userResolver = userResolver;
    }

    /**
     * @param handler           the handler
     * @param userResolver tells who is calling
     */
    public OrmRestServlet(final @NotNull OrmRestHandler<U> handler, final @NotNull RestUserResolver<? extends U> userResolver) {
        this(() -> handler, userResolver);
        this.handler = handler;
    }

    /**
     * Registers a servlet in a servlet context, e.g. from a {@code ServletContextListener}.
     * @param context           the context
     * @param name              the name of the servlet
     * @param mapping           the URL pattern, e.g. {@code /api/*}
     * @param handlerSupplier   provides the handler, see {@link #OrmRestServlet(Supplier, RestUserResolver)}
     * @param userResolver      tells who is calling
     * @param <U>               the type of the user
     * @return the registration, for further configuration
     */
    public static <U> @NotNull ServletRegistration.Dynamic register(final @NotNull ServletContext context,
                                                                    final @NotNull String name,
                                                                    final @NotNull String mapping,
                                                                    final @NotNull Supplier<OrmRestHandler<U>> handlerSupplier,
                                                                    final @NotNull RestUserResolver<? extends U> userResolver) {
        final ServletRegistration.Dynamic registration = context.addServlet(name, new OrmRestServlet<>(handlerSupplier, userResolver));
        registration.addMapping(mapping);
        return registration;
    }

    /**
     * @param maxBodyChars the largest request body (in characters) that is accepted
     */
    public void setMaxBodyChars(final int maxBodyChars) {
        this.maxBodyChars = maxBodyChars;
    }

    private @NotNull OrmRestHandler<U> handler() {
        OrmRestHandler<U> result = handler;
        if (result == null) {
            synchronized (this) {
                result = handler;
                if (result == null) {
                    result = handlerSupplier.get();
                    handler = result;
                }
            }
        }
        return result;
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final RestResponse answer;
        final String body;
        try {
            body = readBody(request);
        } catch (BodyTooLarge e) {
            write(response, new RestResponse(413, "application/problem+json", Map.of(),
                    "{\"type\":\"about:blank\",\"title\":\"Payload Too Large\",\"status\":413,\"detail\":\"The request body is too large.\"}"));
            return;
        }
        answer = handler().handle(new RestRequest<U>(
                request.getMethod(),
                request.getPathInfo() == null ? "/" : request.getPathInfo(),
                queryOf(request),
                headersOf(request),
                body,
                userResolver.resolve(request)));
        write(response, answer);
    }

    private static Map<String, List<String>> queryOf(final HttpServletRequest request) {
        final Map<String, List<String>> query = new HashMap<>();
        for (final Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            query.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return query;
    }

    private static Map<String, String> headersOf(final HttpServletRequest request) {
        final Map<String, String> headers = new HashMap<>();
        final List<String> names = new ArrayList<>();
        final var enumeration = request.getHeaderNames();
        if (enumeration != null) {
            names.addAll(Collections.list(enumeration));
        }
        for (final String name : names) {
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

    private @Nullable String readBody(final HttpServletRequest request) throws IOException, BodyTooLarge {
        final String method = request.getMethod();
        if (!method.equalsIgnoreCase("POST") && !method.equalsIgnoreCase("PUT") && !method.equalsIgnoreCase("PATCH")) {
            return null;
        }
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name()); // JSON is UTF-8 unless it says otherwise
        }
        final StringBuilder text = new StringBuilder();
        try (Reader reader = new BufferedReader(request.getReader())) {
            final char[] buffer = new char[8192];
            int count;
            while ((count = reader.read(buffer)) >= 0) {
                text.append(buffer, 0, count);
                if (text.length() > maxBodyChars) {
                    throw new BodyTooLarge();
                }
            }
        }
        return text.toString();
    }

    private static void write(final HttpServletResponse response, final RestResponse answer) throws IOException {
        response.setStatus(answer.status());
        response.setHeader("X-Content-Type-Options", "nosniff");
        answer.headers().forEach(response::setHeader);
        if (answer.body() != null) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(answer.contentType());
            response.getWriter().write(answer.body());
        }
    }

    private static final class BodyTooLarge extends Exception {
        BodyTooLarge() {
            super("body too large", null, false, false);
        }
    }
}
