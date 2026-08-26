package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.scopes.Scope;

public interface ModuleScopeAware {

    /**
     * Set the module scope of the (presumably) function.
     * @param moduleScope set the module scope
     */
    void setModuleScope(Scope moduleScope);

}
