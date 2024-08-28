package com.eischet.janitor.compiler;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.compiler.ast.statement.Script;
import com.eischet.janitor.lang.JanitorParser;
import org.antlr.v4.runtime.RuleContext;

/**
 * Helper Interface for building a script from a parse tree.
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
        return build(env, module, root, source, false);
    }

    /**
     * Build a script from a parse tree.
     *
     * @param env              the environment
     * @param module           the module
     * @param root             the root of the parse tree
     * @param source           the source code
     * @param verbose          turn on verbose / debugging compiler output
     * @return the script
     */
    static Script build(JanitorEnvironment env, ScriptModule module, JanitorParser.ScriptContext root, String source, boolean verbose) {
        final JanitorAntlrCompiler compiler = new JanitorAntlrCompiler(env, module, verbose, source);
        final RuleContext parseTree = root.getRuleContext();
        return (Script) compiler.visit(parseTree);
    }
}
