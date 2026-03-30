package com.eischet.janitor.maven.env;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.commons.FilesModule;
import com.eischet.janitor.commons.OperatingSystemModule;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.generator.GeneratorModule;
import com.eischet.janitor.modules.brrr.BrrrModule;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.eischet.janitor.runtime.JanitorFormattingLocale;
import com.eischet.janitor.runtime.modules.CollectionsModule;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.util.Locale;

public class MavenScriptingEnv extends JanitorDefaultEnvironment {


    private final SystemStreamLog log;

    public MavenScriptingEnv() {
        super(new JanitorFormattingLocale(Locale.US));
        this.log = new SystemStreamLog();

        addModule(FilesModule.REGISTRATION);
        addModule(CollectionsModule.REGISTRATION);
        addModule(OperatingSystemModule.REGISTRATION);
        addModule(GeneratorModule.REGISTRATION);
        addModule(HttpClientModule.REGISTRATION);
        addModule(BrrrModule.REGISTRATION);
    }

    @Override
    public void warn(final String message) {
        log.warn(message);
    }

    public JanitorRuntime newRuntime() {
        return new MavenScriptingRuntime(this);
    }

    public static MavenScriptingEnv INSTANCE = new MavenScriptingEnv();

}
