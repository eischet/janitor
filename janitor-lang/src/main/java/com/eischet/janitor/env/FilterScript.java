/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor.env;

import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.SLFLoggingRuntime;

import java.util.function.Consumer;

/**
 * A filter predicate implemented as a script.
 * This covers one majore use case of the Janitor language: applying user-supplied filters to Java streams/collections.
 * @see FilterPredicate
 */
public class FilterScript implements FilterPredicate {

    private static final JanitorLogger log = JanitorLogger.getLogger(FilterScript.class);

    private final RunnableScript script;
    private final SLFLoggingRuntime runtime;
    private final Consumer<Scope> globalsProvider;
    private final String name;

    public FilterScript(final JanitorEnvironment env, final String name, final String source, final Consumer<Scope> globalsProvider) throws JanitorCompilerException {
        this.name = name;
        runtime = new SLFLoggingRuntime(env, log);
        this.globalsProvider = globalsProvider;
        script = new JanitorScript(runtime, "filter", source);
    }


    @Override
    public boolean test(final JanitorObject t) {
        try {
            return JBool.TRUE == script.run(g -> {
                if (globalsProvider != null) {
                    globalsProvider.accept(g);
                }
                g.bind("value", t);
                g.setImplicitObjectProvider(t.asImplicitObjectProvider());
            });
        } catch (JanitorRuntimeException e) {
            log.warn("{}: filter script error: {}", name, e.getMessage());
            return false;
        }
    }

}
