// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.rest.OrmApiModelTestCase.Category;
import com.eischet.janitor.orm.rest.OrmApiModelTestCase.TestCollection;
import com.eischet.janitor.orm.rest.servlet.OrmRestServlet;
import com.eischet.janitor.orm.rest.servlet.RestUserResolver;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** The servlet translates between the servlet API and the handler; the handler itself is tested elsewhere. */
public class OrmRestServletTestCase extends JanitorTest {

    private TestCollection collection;
    private final List<Object> seenPrincipals = new ArrayList<>();
    private OrmRestServlet<Object> servlet;

    @BeforeEach
    void setUp() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        collection = new TestCollection(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_category start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_tag start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_item start with 1"));
            conn.update(UpdateStatement.of("create table test_category (category_id bigint primary key, category_key varchar(20), category_name varchar(50))"));
            conn.update(UpdateStatement.of("create table test_tag (tag_id bigint primary key, tag_name varchar(50))"));
            conn.update(UpdateStatement.of("""
                    create table test_item (
                      item_id bigint primary key, item_key varchar(30), item_name varchar(80), item_code varchar(5), item_secret varchar(40),
                      item_rank integer, modified_at timestamp, version_no bigint, category_id bigint, tag_id bigint)"""));
            final Category tools = new Category();
            tools.setKey("tools");
            tools.setName("Tools");
            collection.getCategoryDao().insert(conn, tools);
        });
        servlet = new OrmRestServlet<Object>(OrmRestHandler.of(collection, (principal, entity, operation) -> {
            seenPrincipals.add(principal);
            return "alice".equals(principal);
        }), request -> request.getHeader("X-User"));
    }

    // --- stubs for the servlet API ---

    private static final class Call {
        final Map<String, String> requestHeaders = new HashMap<>();
        final Map<String, String[]> parameters = new HashMap<>();
        String method = "GET";
        String pathInfo;
        String body;
        String requestEncoding;

        int status = 200;
        final Map<String, String> responseHeaders = new HashMap<>();
        String contentType;
        String responseEncoding;
        final StringWriter out = new StringWriter();

        HttpServletRequest request() {
            return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getMethod" -> this.method;
                case "getPathInfo" -> pathInfo;
                case "getParameterMap" -> parameters;
                case "getHeaderNames" -> Collections.enumeration(requestHeaders.keySet());
                case "getHeader" -> requestHeaders.get((String) args[0]);
                case "getCharacterEncoding" -> requestEncoding;
                case "setCharacterEncoding" -> {
                    requestEncoding = (String) args[0];
                    yield null;
                }
                case "getReader" -> new BufferedReader(new StringReader(body == null ? "" : body));
                case "getUserPrincipal" -> throw new AssertionError("the servlet must not look at the container's principal");
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }

        HttpServletResponse response() {
            return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{HttpServletResponse.class}, (proxy, method, args) -> switch (method.getName()) {
                case "setStatus" -> {
                    status = (Integer) args[0];
                    yield null;
                }
                case "setHeader" -> {
                    responseHeaders.put((String) args[0], (String) args[1]);
                    yield null;
                }
                case "setContentType" -> {
                    contentType = (String) args[0];
                    yield null;
                }
                case "setCharacterEncoding" -> {
                    responseEncoding = (String) args[0];
                    yield null;
                }
                case "getWriter" -> new PrintWriter(out);
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }
    }

    private Call call(final String method, final String pathInfo, final @Nullable String user) throws Exception {
        final Call call = new Call();
        call.method = method;
        call.pathInfo = pathInfo;
        if (user != null) {
            call.requestHeaders.put("X-User", user);
        }
        return call;
    }

    private void run(final Call call) throws Exception {
        final var response = call.response();
        servlet.service(call.request(), response);
        response.getWriter().flush();
    }

    // --- tests ---

    @Test
    public void answersARequestWithStatusHeadersAndJsonBody() throws Exception {
        final Call call = call("GET", "/categories/by-key/tools", "alice");
        run(call);
        assertEquals(200, call.status);
        assertEquals("application/json", call.contentType);
        assertEquals("UTF-8", call.responseEncoding);
        assertEquals("nosniff", call.responseHeaders.get("X-Content-Type-Options"));
        assertTrue(call.out.toString().replaceAll("\\s", "").contains("\"key\":\"tools\""), call.out.toString());
    }

    @Test
    public void thePrincipalComesFromTheResolverAndNotFromTheContainer() throws Exception {
        run(call("GET", "/categories", "alice")); // the stub throws AssertionError if getUserPrincipal() is called
        assertEquals(List.of("alice"), seenPrincipals);
        final Call stranger = call("GET", "/categories", "mallory");
        run(stranger);
        assertEquals(403, stranger.status);
        assertEquals("application/problem+json", stranger.contentType);
        final Call anonymous = call("GET", "/categories", null);
        run(anonymous);
        assertEquals(401, anonymous.status, "nobody is authenticated: asked to authenticate, not forbidden");
        assertNull(seenPrincipals.get(seenPrincipals.size() - 1), "no user: the resolver answers null");
    }

    @Test
    public void passesQueryParametersAndHeadersOn() throws Exception {
        final Call call = call("GET", "/categories", "alice");
        call.parameters.put("filter", new String[]{"{\"field\":\"key\",\"operator\":\"eq\",\"valueString\":\"nobody\"}"});
        run(call);
        assertEquals(200, call.status);
        assertTrue(call.out.toString().replaceAll("\\s", "").contains("\"total\":0"), call.out.toString());
    }

    @Test
    public void passesTheBodyOfPostsOnAndReadsItAsUtf8ByDefault() throws Exception {
        final Call call = call("POST", "/categories/search", "alice");
        call.body = "{\"filter\":{\"field\":\"key\",\"operator\":\"eq\",\"valueString\":\"tools\"}}";
        run(call);
        assertEquals("UTF-8", call.requestEncoding);
        assertEquals(200, call.status);
        assertTrue(call.out.toString().replaceAll("\\s", "").contains("\"total\":1"), call.out.toString());
    }

    @Test
    public void aMissingPathInfoIsTheRoot() throws Exception {
        final Call call = call("GET", null, "alice");
        run(call);
        assertEquals(404, call.status);
    }

    @Test
    public void unsupportedMethodsGetTheAnswerOfTheHandler() throws Exception {
        final Call call = call("DELETE", "/categories/1", "alice");
        run(call);
        assertEquals(405, call.status);
        assertEquals("GET", call.responseHeaders.get("Allow"));
    }

    @Test
    public void tooLargeBodiesAreRefused() throws Exception {
        servlet.setMaxBodyChars(10);
        final Call call = call("POST", "/categories/search", "alice");
        call.body = "{\"filter\":{\"field\":\"key\",\"operator\":\"eq\",\"valueString\":\"tools\"}}";
        run(call);
        assertEquals(413, call.status);
        assertEquals("application/problem+json", call.contentType);
    }

    @Test
    public void theHandlerIsCreatedOnFirstUse() throws Exception {
        final AtomicInteger created = new AtomicInteger();
        final OrmRestServlet<Object> lazy = new OrmRestServlet<Object>(() -> {
            created.incrementAndGet();
            return OrmRestHandler.of(collection, RestPolicy.allowAll());
        }, RestUserResolver.anonymous());
        assertEquals(0, created.get());
        for (int i = 0; i < 2; i++) {
            final Call call = call("GET", "/categories", null);
            final var response = call.response();
            lazy.service(call.request(), response);
            assertEquals(200, call.status);
        }
        assertEquals(1, created.get());
    }

    @Test
    public void registersItselfInAServletContext() {
        final List<String> log = new ArrayList<>();
        final ServletRegistration.Dynamic registration = (ServletRegistration.Dynamic) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ServletRegistration.Dynamic.class}, (proxy, method, args) -> {
            if (method.getName().equals("addMapping")) {
                log.add("mapping " + ((String[]) args[0])[0]);
                return Collections.emptySet();
            }
            throw new UnsupportedOperationException(method.getName());
        });
        final ServletContext context = (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ServletContext.class}, (proxy, method, args) -> {
            if (method.getName().equals("addServlet") && args[1] instanceof OrmRestServlet) {
                log.add("servlet " + args[0]);
                return registration;
            }
            throw new UnsupportedOperationException(method.getName());
        });
        OrmRestServlet.register(context, "api", "/api/*", () -> OrmRestHandler.of(collection, RestPolicy.allowAll()), RestUserResolver.anonymous());
        assertEquals(List.of("servlet api", "mapping /api/*"), log);
    }
}
