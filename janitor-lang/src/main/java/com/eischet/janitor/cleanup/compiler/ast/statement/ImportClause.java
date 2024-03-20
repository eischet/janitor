package com.eischet.janitor.cleanup.compiler.ast.statement;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNotImplementedException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorModule;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Identifier;
import com.eischet.janitor.cleanup.compiler.ast.expression.QualifiedName;
import com.eischet.janitor.cleanup.compiler.ast.statement.controlflow.JanitorControlFlowException;

public class ImportClause extends Statement {
    private final String module;
    private final Identifier alias;
    private final QualifiedName qname;

    public ImportClause(final Location location, final QualifiedName qname, final Identifier alias) {
        super(location);
        this.qname = qname;
        this.alias = alias;
        this.module = null;
    }

    public ImportClause(final Location location, final String module, final Identifier alias) {
        super(location);
        this.module = module;
        this.alias = alias;
        this.qname = null;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        // Load qualified modules:
        if (qname != null) {
            if (qname.getParts().size() == 1) {
                final JanitorModule m = runningScript.getRuntime().getModuleByQualifier(runningScript, qname.getParts().get(0));
                if (alias == null) {
                    runningScript.getMainScope().bind(qname.getParts().get(0), m);
                } else {
                    runningScript.getMainScope().bind(alias.getText(), m);
                }
                // alt: runningScript.getMainScope().bind(alias == null ? qname.getParts().get(0) : alias, module);
                return;
            }
        }
        // Load string-based modules (implementation specific):
        if (module != null) {
            final JanitorModule m = runningScript.getRuntime().getModuleByStringName(runningScript, module);
            runningScript.getMainScope().bind(alias.getText(), m);
            return;
        }
        throw new JanitorNotImplementedException(runningScript, "invalid import clause: either a qualified name or a module name string is required");
    }

}
