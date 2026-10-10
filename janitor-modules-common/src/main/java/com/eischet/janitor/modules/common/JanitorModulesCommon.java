package com.eischet.janitor.modules.common;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.generator.GeneratorModule;
import com.eischet.janitor.modules.brrr.BrrrModule;
import com.eischet.janitor.modules.files.FilesModule;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.eischet.janitor.modules.janitor.JanitorInternalsModule;
import com.eischet.janitor.modules.os.OperatingSystemModule;
import com.eischet.janitor.runtime.modules.CollectionsModule;

/** Registers the commonly used modules with an environment. */
public class JanitorModulesCommon {
    /**
     * Adds the common modules to an environment.
     * @param env the environment
     * @param includingNonSandboxed true to include modules that give scripts access to the file system and the operating system, which must not be used for untrusted scripts
     */
    public static void registerCommonModules(JanitorEnvironment env, boolean includingNonSandboxed) {
        env.addModule(JanitorInternalsModule.REGISTRATION);
        env.addModule(CollectionsModule.REGISTRATION);
        env.addModule(GeneratorModule.REGISTRATION);
        env.addModule(HttpClientModule.REGISTRATION);
        env.addModule(BrrrModule.REGISTRATION);
        if (includingNonSandboxed) {
            env.addModule(FilesModule.REGISTRATION);
            env.addModule(OperatingSystemModule.REGISTRATION);
        }
    }
}
