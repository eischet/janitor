// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime.modules;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/**
 * A module with factory functions for collections: {@code Set}, {@code List} and {@code Map}. Like classes in Java, they
 * are capitalized; {@code set}, {@code list} and {@code map} remain as aliases.
 */
public class CollectionsModule extends JanitorComposed<CollectionsModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("collections", CollectionsModule::new);

    private static final DispatchTable<CollectionsModule> DISPATCH = new DispatchTable<>(CollectionsModule::new, false);

    static {
        // Every HELP text starts with the call form, "name(arguments): ", which the language reference relies on.
        DISPATCH.addMethod("Set", (self, process, arguments) -> Janitor.set(arguments.getList().stream()))
                .setMetaData(Janitor.MetaData.HELP, "Set(values...): Returns a new set with the values. Also available as set(...).");
        DISPATCH.addMethod("List", (self, process, arguments) -> Janitor.list(arguments.getList()))
                .setMetaData(Janitor.MetaData.HELP, "List(values...): Returns a new list with the values. Also available as list(...).");
        DISPATCH.addMethod("Map", (self, process, arguments) -> {
            arguments.require(0);
            return Janitor.map();
        }).setMetaData(Janitor.MetaData.HELP, "Map(): Returns a new, empty map; takes no arguments. Also available as map().");
        DISPATCH.addAlias("set", "Set");
        DISPATCH.addAlias("list", "List");
        DISPATCH.addAlias("map", "Map");
    }

    public CollectionsModule() {
        super(DISPATCH);
    }
}
