// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorNotImplementedException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Import clauses, which are part of import statements minus the import keyword: import foo; import bar as baz; import "custom" as stuff;.
 * @see ImportStatement
 */
public class ImportClause extends Statement implements JsonExportableObject {
    private final String module;
    private final Identifier alias;
    private final Identifier name;

    /**
     * Constructor for imports of registered modules: import foo; import bar as baz;.
     *
     * @param location where
     * @param name     the name under which the module is registered
     * @param alias    alias, or null to bind the module under its name
     */
    public ImportClause(final Location location, final Identifier name, final Identifier alias) {
        super(location);
        this.name = name;
        this.alias = alias;
        this.module = null;
    }

    /**
     * Constructor for imports with string names: import "foo" as bar;.
     *
     * @param location where
     * @param module   module name
     * @param alias    alias
     */
    public ImportClause(final Location location, final String module, final Identifier alias) {
        super(location);
        this.module = module;
        this.alias = alias;
        this.name = null;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        // Load registered modules:
        if (name != null) {
            final JanitorModule m = process.getEnvironment().getModuleByQualifier(process, name.getText());
            process.getMainScope().bind(process, alias == null ? name.getText() : alias.getText(), m);
            return;
        }
        // Load string-based modules (implementation specific):
        if (module != null) {
            final JanitorModule m = process.getEnvironment().getModuleByStringName(process, module);
            process.getMainScope().bind(process, alias.getText(), m);
            return;
        }
        throw new JanitorNotImplementedException(process, "invalid import clause: either a module name or a module name string is required");
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("module", module)
                .optional("alias", alias)
                .optional("name", name)
                .endObject();

    }
}
