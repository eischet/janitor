package com.eischet.janitor.cleanup.impl;

import com.eischet.janitor.cleanup.api.api.JanitorEngine;
import com.eischet.janitor.cleanup.api.api.JanitorRuntime;
import com.eischet.janitor.cleanup.api.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.cleanup.compiler.JanitorCompiler;
import com.eischet.janitor.cleanup.compiler.JanitorCompilerSettings;
import com.eischet.janitor.cleanup.runtime.JanitorScript;

public class JanitorEngineSimple implements JanitorEngine {
    @Override
    public JanitorScript compile(final JanitorRuntime runtime, final String moduleName, final String source, final boolean checking) throws JanitorCompilerException {
        return new JanitorScript(runtime, moduleName, source, checking);
    }

    @Override
    public JanitorCompiler newCompiler(final JanitorCompilerSettings settings) {
        return null;
    }

    @Override
    public JanitorRuntime newRuntime() {
        return null;
    }
}
