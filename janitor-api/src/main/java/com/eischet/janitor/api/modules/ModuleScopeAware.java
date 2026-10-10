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
