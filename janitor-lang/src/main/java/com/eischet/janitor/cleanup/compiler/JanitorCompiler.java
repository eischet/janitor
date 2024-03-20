package com.eischet.janitor.cleanup.compiler;

import com.eischet.janitor.cleanup.api.api.scopes.ScriptModule;
import com.eischet.janitor.cleanup.compiler.ast.statement.Script;
import janitor.lang.JanitorParser;
import org.antlr.v4.runtime.RuleContext;

public interface JanitorCompiler {

    static Script build(ScriptModule module,
                        JanitorParser.ScriptContext root,
                        String source) {
        return build(module, root, source, JanitorCompilerSettings.DEFAUlTS);
    }

    static Script build(ScriptModule module,
                        JanitorParser.ScriptContext root,
                        String source,
                        JanitorCompilerSettings compilerSettings) {
        final JanitorAntlrCompiler compiler = new JanitorAntlrCompiler(module, compilerSettings, source);
        final RuleContext parseTree = root.getRuleContext();
        return (Script) compiler.visit(parseTree);
    }
}
