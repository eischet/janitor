package com.eischet.janitor.tests.cleanup;

import com.eischet.janitor.cleanup.api.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.ScriptModule;
import com.eischet.janitor.cleanup.api.api.types.JNull;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.JanitorCompiler;
import com.eischet.janitor.cleanup.compiler.ast.statement.Script;
import com.eischet.janitor.cleanup.runtime.JanitorScript;
import com.eischet.janitor.cleanup.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.cleanup.runtime.RunningScriptProcess;
import com.eischet.janitor.cleanup.runtime.scope.Scope;
import com.eischet.janitor.cleanup.runtime.types.JCallArgs;
import com.eischet.janitor.cleanup.runtime.types.NativeFunction;
import com.eischet.janitor.cleanup.template.TemplateBlock;
import com.eischet.janitor.cleanup.template.TemplateParser;
import com.eischet.janitor.cleanup.template.TemplateRenderer;
import janitor.lang.JanitorParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JanitorTemplateTestCase {

    private static final Logger log = LoggerFactory.getLogger(JanitorTemplateTestCase.class);

    @Test
    public void dollars() {
        assertEquals("<%= i %>", TemplateParser.replaceDollarVars("${i}"));
    }



    private String templateToScript(final String script, final TemplateRenderer renderer) {
        System.out.println("--- script: " + script);
        final TemplateParser parser = new TemplateParser(script);
        for (TemplateBlock block : parser.getBlocks()) {
            System.out.println(block);
        }
        final String result = renderer.renderBlocks(parser.getBlocks().stream());
        System.out.println("-> result:");
        System.out.println(result);
        return result;
    }

    private String evaluateTemplate(final String templateScript, final Consumer<Scope> vars) throws JanitorRuntimeException, JanitorCompilerException {
        final StringWriter writer = new StringWriter();
        getOutput(templateScript, g -> {
            vars.accept(g);
            g.bind("__OUT__", new NativeFunction("__OUT__") {
                @Override
                public JanitorObject call(JanitorScriptProcess runningScript, JCallArgs arguments) throws JanitorRuntimeException {
                    arguments.getList().stream().forEach(arg -> writer.write(arg.janitorToString()));
                    return JNull.NULL;
                }
            });
        });
        return writer.toString();
    }


    @Test
    public void foo() throws JanitorRuntimeException, JanitorCompilerException {
        templateToScript("hallo", TemplateParser::plainRenderer);
        templateToScript("hallo<%='welt'%>", TemplateParser::plainRenderer);
        templateToScript("hallo <%='schöne'%> welt", TemplateParser::plainRenderer);
        templateToScript("<%-- comment 1 --%>hallo<%-- comment 2 --%>", TemplateParser::plainRenderer);
        final String fooScript = templateToScript("<%-- logic --%><% if (!foo) { %>wrong<% } else { %>right<% } %>", TemplateParser::plainRenderer);
        assertEquals("/* logic */ if (!foo) { __OUT__('''wrong'''); } else { __OUT__('''right'''); } ", fooScript);

        final String fooOutTrue = evaluateTemplate(fooScript, g -> g.bind("foo", true));
        final String fooOutFalse = evaluateTemplate(fooScript, g -> g.bind("foo", false));
        assertEquals("right", fooOutTrue);
        assertEquals("wrong", fooOutFalse);
    }

    private String getOutput(final String scriptSource, final Consumer<Scope> prepareGlobals) throws JanitorCompilerException, JanitorRuntimeException {
        log.info("parsing: " + scriptSource + "\n");
        final JanitorParser.ScriptContext script = JanitorScript.parseScript(scriptSource);
        final ScriptModule module = ScriptModule.unnamed(scriptSource);
        final Script scriptObject = JanitorCompiler.build(module, script, scriptSource);
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();

        final Scope globalScope = Scope.createGlobalScope(module); // new Scope(null, JanitorScript.BUILTIN_SCOPE, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        runningScript.run();
        return runtime.getAllOutput();
    }

    /*
    private String evaluateTemplate(final String expressionSource, final Consumer<Scope> prepareGlobals) throws JanitorParserException, JanitorRuntimeException {
        log.info("parsing: " + expressionSource + "\n");

        final JanitorParser.ScriptContext script = JanitorScript.parseScript(expressionSource);
        final ScriptModule module = ScriptModule.unnamed(expressionSource);
        final IR.Script scriptObject = Compiler.build(module, script, expressionSource);
        final OutputCatchingTestRuntime runtime = new OutputCatchingTestRuntime();

        final Scope globalScope = new Scope(null, null, null);
        prepareGlobals.accept(globalScope);
        final RunningScriptProcess runningScript = new RunningScriptProcess(runtime, globalScope, scriptObject);
        return runningScript.run();
    }

     */


}
