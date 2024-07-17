/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor.env;

import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JBool;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.SLFLoggingRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter predicate implemented as a script.
 * This covers one majore use case of the Janitor language: applying user-supplied filters to Java streams/collections.
 */
public class FilterScript implements FilterPredicate {

    private static final Logger log = LoggerFactory.getLogger(FilterScript.class);

    private final RunnableScript script;
    private final SLFLoggingRuntime runtime;
    private final String name;

    public FilterScript(final JanitorEnvironment env, final String name, final String source) throws JanitorCompilerException {
        this.name = name;
        runtime = new SLFLoggingRuntime(env, log);
        runtime.getCompilerSettings().setRelaxNullPointers(true);
        script = new JanitorScript(runtime, "filter", source);
    }

    @Override
    public boolean test(final JanitorObject t) {
        try {
            return JBool.TRUE == script.run(g -> {
                // g.bind("value", t); // das erwartet man eher, das "implicit object" ist eher abwegig
                g.setImplicitObject(t); // erbt bei QueryResultsRow dann dessen "value"
            });
        } catch (JanitorRuntimeException e) {
            log.warn("{}: filter script error: {}", name, e.getMessage());
            return false;
        }
    }

}
