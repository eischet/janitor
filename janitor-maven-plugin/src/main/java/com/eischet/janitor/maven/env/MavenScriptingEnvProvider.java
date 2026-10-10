// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.maven.env;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorEnvironmentProvider;
import com.google.auto.service.AutoService;

/** Makes the Maven scripting environment the current environment, with a high priority. */
@AutoService(JanitorEnvironmentProvider.class)
public class MavenScriptingEnvProvider implements JanitorEnvironmentProvider {
    @Override
    public JanitorEnvironment getCurrentEnvironment() {
        return MavenScriptingEnv.INSTANCE;
    }

    @Override
    public int priority() {
        return 1000;
    }
}
