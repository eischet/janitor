package com.eischet.janitor.compiler.ast;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.Nullable;

/**
 * Base interface for all AST members.
 */
public interface Ast {
    /**
     * Get the location of this AST node.
     * @return location
     */
    @Nullable Location getLocation();
}
