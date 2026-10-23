// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Properties marked with {@link Janitor.MetaData#CONFIDENTIAL} are written to JSON by default, and left out when the write options say so.
 */
public class JsonConfidentialFieldsTestCase extends JanitorTest {

    private static final JsonWriteOptions OMIT = JsonWriteOptions.COMPACT.withOmitConfidential(true);

    @Test
    public void writtenByDefault() throws JsonException {
        final Login login = new Login();
        assertEquals("{\"user\":\"stefan\",\"password\":\"geheim\",\"retries\":3}", Login.DISPATCH.writeToJson(login));
        assertEquals("{\"user\":\"stefan\",\"password\":\"geheim\",\"retries\":3}", Janitor.writeJson(JsonWriteOptions.COMPACT, out -> Login.DISPATCH.writeToJson(out, login)));
    }

    @Test
    public void omittedOnRequest() throws JsonException {
        final Login login = new Login();
        assertEquals("{\"user\":\"stefan\",\"retries\":3}", Janitor.writeJson(OMIT, out -> Login.DISPATCH.writeToJson(out, login)));
    }

    @Test
    public void omittedFromParentTables() throws JsonException {
        final AdminLogin admin = new AdminLogin();
        assertEquals("{\"user\":\"stefan\",\"password\":\"geheim\",\"retries\":3,\"token\":\"abc\",\"role\":\"admin\"}",
                Janitor.writeJson(JsonWriteOptions.COMPACT, out -> AdminLogin.DISPATCH.writeToJson(out, admin)));
        assertEquals("{\"user\":\"stefan\",\"retries\":3,\"role\":\"admin\"}", Janitor.writeJson(OMIT, out -> AdminLogin.DISPATCH.writeToJson(out, admin)));
    }

    @Test
    public void omittedFromSchema() throws JsonException {
        assertEquals("{\"type\":\"object\",\"properties\":{\"user\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"retries\":{\"type\":\"number\"}}}",
                Janitor.writeJson(JsonWriteOptions.COMPACT, Login.DISPATCH::writeSchemaToJson));
        assertEquals("{\"type\":\"object\",\"properties\":{\"user\":{\"type\":\"string\"},\"retries\":{\"type\":\"number\"}}}",
                Janitor.writeJson(OMIT, Login.DISPATCH::writeSchemaToJson));
    }

    @Test
    public void scriptsStillSeeTheProperty() throws Exception {
        assertEquals("geheim", evaluate("login.password", g -> g.bind("login", new Login())).janitorGetHostValue());
    }

    @Test
    public void omittedFieldsKeepTheirValueWhenReadBack() throws JsonException {
        final Login login = new Login();
        login.password = "anders";
        Login.DISPATCH.readFromJson(() -> login, Janitor.writeJson(OMIT, out -> Login.DISPATCH.writeToJson(out, new Login())));
        assertEquals("anders", login.password);
    }

    private static class Login extends JanitorComposed<Login> {
        private static final DispatchTable<Login> DISPATCH = new DispatchTable<>(null);

        static {
            DISPATCH.addStringProperty("user", self -> self.user, (self, value) -> self.user = value);
            DISPATCH.addStringProperty("password", self -> self.password, (self, value) -> self.password = value)
                    .setMetaData(Janitor.MetaData.CONFIDENTIAL, true);
            DISPATCH.addIntegerProperty("retries", self -> self.retries, (self, value) -> self.retries = value);
        }

        String user = "stefan";
        String password = "geheim";
        int retries = 3;

        Login() {
            super(DISPATCH);
        }

        Login(final Dispatcher<Login> dispatch) {
            super(dispatch);
        }
    }

    private static class AdminLogin extends Login {
        private static final DispatchTable<AdminLogin> DISPATCH = new DispatchTable<>(Login.DISPATCH, self -> self, true);

        static {
            DISPATCH.addStringProperty("token", self -> self.token, (self, value) -> self.token = value)
                    .setMetaData(Janitor.MetaData.CONFIDENTIAL, true);
            DISPATCH.addStringProperty("role", self -> self.role, (self, value) -> self.role = value);
        }

        String token = "abc";
        String role = "admin";

        AdminLogin() {
            super(Dispatcher.inherit(Login.DISPATCH, DISPATCH));
        }
    }

}
