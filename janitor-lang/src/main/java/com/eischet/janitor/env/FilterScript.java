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
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.SLFLoggingRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * A filter predicate implemented as a script.
 * This covers one majore use case of the Janitor language: applying user-supplied filters to Java streams/collections.
 * @see FilterPredicate
 */
public class FilterScript implements FilterPredicate {

    private static final Logger log = LoggerFactory.getLogger(FilterScript.class);

    private final RunnableScript script;
    private final SLFLoggingRuntime runtime;
    private final Consumer<Scope> globalsProvider;
    private final String name;
    private final boolean explicitBind;

    @Deprecated
    public FilterScript(final JanitorEnvironment env, final String name, final String source) throws JanitorCompilerException {
        this.name = name;
        runtime = new SLFLoggingRuntime(env, log);
        runtime.getCompilerSettings().setRelaxNullPointers(true);
        script = new JanitorScript(runtime, "filter", source);
        this.globalsProvider = null;
        this.explicitBind = false; // TODO: remove this flag once this constructor has been removed!
    }

    public FilterScript(final JanitorEnvironment env, final String name, final String source, final Consumer<Scope> globalsProvider) throws JanitorCompilerException {
        this.name = name;
        runtime = new SLFLoggingRuntime(env, log);
        this.globalsProvider = globalsProvider;
        runtime.getCompilerSettings().setRelaxNullPointers(true);
        script = new JanitorScript(runtime, "filter", source);
        explicitBind = true;
    }


    @Override
    public boolean test(final JanitorObject t) {
        try {
            return JBool.TRUE == script.run(g -> {
                if (globalsProvider != null) {
                    globalsProvider.accept(g);
                }
                if (explicitBind) {
                    g.bind("value", t);
                } else {
                    // historic error - using the implicit object instead here was a bad idea
                    // must change the existing (closed source) client code first before this can be removed...
                    g.setImplicitObject(t);
                }
            });
        } catch (JanitorRuntimeException e) {
            log.warn("{}: filter script error: {}", name, e.getMessage());
            return false;
        }
    }

}
