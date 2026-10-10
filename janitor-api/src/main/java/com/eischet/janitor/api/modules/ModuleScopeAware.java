// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.scopes.Scope;

/** Implemented by objects, typically functions, that need to know the scope of the module that defines them. */
public interface ModuleScopeAware {

    /**
     * Set the module scope of the (presumably) function.
     * @param moduleScope set the module scope
     */
    void setModuleScope(Scope moduleScope);

}
