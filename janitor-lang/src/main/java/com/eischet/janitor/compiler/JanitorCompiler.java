package com.eischet.janitor.compiler;

import com.eischet.janitor.api.JanitorCompilerSettings;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.lang.JanitorParser;
import org.antlr.v4.runtime.RuleContext;

/**
 * Helper Interface for building a script from a parse tree.
 * TODO: I'd rather have this in the Environment class instead of here, but didn't dare to refactor all the stuff.
 */
public interface JanitorCompiler {

    /**
     * Build a script from a parse tree.
     *
     * @param env    the environment
     * @param module the module
     * @param root   the root of the parse tree
     * @param source the source code
     * @return the script
     */
    static Script build(JanitorEnvironment env, ScriptModule module, JanitorParser.ScriptContext root, String source) {
        return build(env, module, root, source, JanitorCompilerSettings.DEFAUlTS);
    }

    /**
     * Build a script from a parse tree.
     *
     * @param env              the environment
     * @param module           the module
     * @param root             the root of the parse tree
     * @param source           the source code
     * @param compilerSettings the compiler settings
     * @return the script
     */
    static Script build(JanitorEnvironment env, ScriptModule module, JanitorParser.ScriptContext root, String source, JanitorCompilerSettings compilerSettings) {
        final JanitorAntlrCompiler compiler = new JanitorAntlrCompiler(env, module, compilerSettings, source);
        final RuleContext parseTree = root.getRuleContext();
        return (Script) compiler.visit(parseTree);
    }
}
