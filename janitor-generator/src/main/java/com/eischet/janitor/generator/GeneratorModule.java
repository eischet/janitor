// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.generator;

import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** The script module "generator", which lets scripts generate Java code. */
public class GeneratorModule extends JanitorComposed<GeneratorModule> implements JanitorModule {

    public static final DispatchTable<GeneratorModule> DISPATCH = new DispatchTable<>(GeneratorModule::new);

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("generator", GeneratorModule::new);

    static {
        DISPATCH.addMethod("Generator", (self, process, arguments) -> arguments.applyOptionalMap(process, new Generator()));
    }

    public GeneratorModule() {
        super(DISPATCH);
    }

}
