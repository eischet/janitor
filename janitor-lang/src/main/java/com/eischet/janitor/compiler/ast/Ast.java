package com.eischet.janitor.compiler.ast;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.Nullable;

public interface Ast {
    @Nullable Location getLocation();
}
