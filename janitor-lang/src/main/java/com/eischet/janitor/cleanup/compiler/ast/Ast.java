package com.eischet.janitor.cleanup.compiler.ast;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import org.jetbrains.annotations.Nullable;

public interface Ast {
    @Nullable Location getLocation();
}
