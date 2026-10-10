// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.maven.env;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.modules.common.JanitorModulesCommon;
import com.eischet.janitor.modules.commonmark.CommonMarkModule;
import com.eischet.janitor.modules.janitor.JanitorInternalsModule;
import com.eischet.janitor.runtime.JanitorFormattingLocale;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.util.Locale;

/** The Janitor environment for scripts that run in a Maven build. */
public class MavenScriptingEnv extends JanitorDefaultEnvironment {


    private final SystemStreamLog log;

    public MavenScriptingEnv() {
        super(new JanitorFormattingLocale(Locale.US));
        this.log = new SystemStreamLog();

        JanitorInternalsModule.host = JanitorInternalsModule.HOST_MAVEN;

        JanitorModulesCommon.registerCommonModules(this, true);
        this.addModule(CommonMarkModule.REGISTRATION);

    }

    @Override
    public void warn(final String message) {
        log.warn(message);
    }

    /**
     * @return a new runtime for running a script in a Maven build
     */
    public JanitorRuntime newRuntime() {
        return new MavenScriptingRuntime(this);
    }

    public static MavenScriptingEnv INSTANCE = new MavenScriptingEnv();

}
