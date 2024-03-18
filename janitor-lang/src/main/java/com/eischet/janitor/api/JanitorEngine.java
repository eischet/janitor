package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.compiler.JanitorCompiler;
import com.eischet.janitor.compiler.JanitorCompilerSettings;
import com.eischet.janitor.runtime.JanitorScript;

public interface JanitorEngine {

    JanitorScript compile(final JanitorRuntime runtime,
                          final String moduleName,
                          final String source,
                          final boolean checking) throws JanitorCompilerException;


    JanitorCompiler newCompiler(JanitorCompilerSettings settings);
    JanitorRuntime newRuntime();
}
