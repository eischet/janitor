// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.tooling;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.api.scopes.NameDoc;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.functions.tree.JInspectableFunction;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes the top-level scope of a module as a dispatcher, so that suggestions can treat it like any other object: its functions
 * become methods with their parameter names, its values properties with the type of what they hold; private names
 * ({@link ScriptModule#isExported}) are left out. Only for tools; the dispatcher dispatches nothing.
 */
public final class ScopeDescription {

    private ScopeDescription() {
    }

    /**
     * @param name  the name of the module, the class name of the description
     * @param scope the module's top-level scope; only read
     * @return a dispatcher describing the members of the scope
     */
    public static @NotNull Dispatcher<?> of(final @NotNull String name, final @NotNull Scope scope) {
        final DispatchTable<JanitorObject> table = new DispatchTable<>(false);
        table.setMetaData(Janitor.MetaData.CLASS, name);
        for (final String member : scope.dir()) {
            final JanitorObject value = scope.retrieveLocal(member);
            if (value == null || !ScriptModule.isExported(member)) {
                continue;
            }
            final NameDoc doc = scope.getNameDoc(member);
            final String documented = doc == null || doc.text() == null ? null : doc.render();
            final String help = documented != null ? documented
                    : value instanceof HasMetaData hasMetaData ? hasMetaData.getMetaData(Janitor.MetaData.HELP) : null;
            if (value instanceof JCallable) {
                final String parameters = value instanceof JInspectableFunction function ? String.join(", ", function.getParameterNames()) : "...";
                final String signature = doc != null && doc.signature() != null ? doc.signature() : member + "(" + parameters + ")";
                table.addMethod(member, (self, process, arguments) -> Janitor.NULL)
                        .setMetaData(Janitor.MetaData.HELP, help != null ? help : signature + ": A function of the module " + name + ".");
            } else {
                final Dispatcher<?> dispatcher = dispatcherOf(value);
                table.addObjectProperty(member, self -> Janitor.NULL)
                        .setMetaData(Janitor.MetaData.HELP, help != null ? help : member + ": A value of the module " + name + ", " + value.janitorClassName() + ".")
                        .setMetaData(Janitor.MetaData.RETURNS, dispatcher == null ? null : new ScriptType.Of(value.janitorClassName(), dispatcher));
            }
        }
        return table;
    }

    private static @Nullable Dispatcher<?> dispatcherOf(final JanitorObject object) {
        if (object instanceof HasDispatcher<?> hasDispatcher) {
            return hasDispatcher.getDispatcher();
        }
        if (object instanceof JanitorWrapper<?> wrapper) {
            return wrapper.getDispatcher();
        }
        return null;
    }
}
