package com.eischet.janitor.cleanup.api.api;

import com.eischet.janitor.cleanup.api.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.cleanup.compiler.JanitorCompiler;
import com.eischet.janitor.cleanup.compiler.JanitorCompilerSettings;
import com.eischet.janitor.cleanup.runtime.JanitorScript;

public interface JanitorEngine {

    JanitorScript compile(final JanitorRuntime runtime,
                          final String moduleName,
                          final String source,
                          final boolean checking) throws JanitorCompilerException;


    JanitorCompiler newCompiler(JanitorCompilerSettings settings);
    JanitorRuntime newRuntime();
}
