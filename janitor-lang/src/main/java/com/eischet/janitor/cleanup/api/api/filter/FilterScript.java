/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor.cleanup.api.api.filter;

import com.eischet.janitor.cleanup.api.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JBool;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.runtime.JanitorScript;
import com.eischet.janitor.cleanup.runtime.SLFLoggingRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterScript implements FilterPredicate {

    private static final Logger log = LoggerFactory.getLogger(FilterScript.class);

    private final JanitorScript script;
    private final SLFLoggingRuntime runtime;
    private final String name;


    private FilterScript(final String name, final String source) throws JanitorCompilerException {
        this.name = name;
        runtime = new SLFLoggingRuntime(log);
        runtime.getCompilerSettings().setRelaxNullPointers(true);
        script = new JanitorScript(runtime, "filter", source);
    }

    @Override
    public boolean test(final JanitorObject t) {
        try {
            return JBool.TRUE == script.run(g -> {
                g.setImplicitObject(t); // erbt bei QueryResultsRow dann dessen "value"
            });
        } catch (JanitorRuntimeException e) {
            log.warn("{}: filter script error: {}", name, e.getMessage());
            return false;
        }
    }

    private static FilterPredicate numb() {
        return new FilterPredicate() {
            @Override
            public FilterScript getFilterScript() {
                return null;
            }

            @Override
            public boolean test(final JanitorObject x) {
                return true;
            }
        };
    }

    public static FilterPredicate of(final String name, final String code) {
        if (code == null || code.isEmpty() || code.trim().isEmpty()) {
            return numb();
        }
        try {
            return new FilterScript(name, code);
        } catch (JanitorCompilerException JanitorParserException) {
            log.warn("error compiling filter: {}", code, JanitorParserException);
            return numb();
        }
    }

    @Override
    public FilterScript getFilterScript() {
        return this;
    }
}
