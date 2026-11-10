// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.scopes;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

/**
 * A scope that offers the attributes of an object as names, e.g. the fields of the object a filter script checks, or the
 * values of a template. The object computes them when a script reads them, through {@code janitorGetAttribute}.
 * <p>
 * The object has no variables of its own for scripts to change: an assignment to one of its names shadows the name in the
 * script's main scope, so {@code ticket = other;} makes {@code ticket} the other value for the rest of the script without
 * touching the object. Changing an attribute, as in {@code ticket.status = 2}, changes the object as usual.
 * </p>
 */
public class ObjectScope implements Scope {

    private final @NotNull JanitorObject object;
    private final @Nullable Scope parent;

    private ObjectScope(final @NotNull JanitorObject object, final @Nullable Scope parent) {
        this.object = object;
        this.parent = parent;
    }

    /**
     * @param object the object whose attributes become names; a map offers its keys, but none of its methods
     * @param parent the scope that is asked for other names
     * @return the scope
     */
    public static @NotNull ObjectScope of(final @NotNull JanitorObject object, final @Nullable Scope parent) {
        return new ObjectScope(object instanceof JMap map ? map.readonlyView() : object, parent);
    }

    /**
     * The scope for an object that a script runs for, e.g. the one a filter checks or the values of a template: its
     * attributes, then the environment's template globals (see {@link JanitorEnvironment#getTemplateGlobals()}).
     *
     * @param env    the environment
     * @param object the object, or null for the template globals alone
     * @param parent the scope that is asked for other names
     * @return the scope, or the parent if there is neither an object nor template globals
     */
    public static @Nullable Scope withTemplateGlobals(final @NotNull JanitorEnvironment env, final @Nullable JanitorObject object, final @Nullable Scope parent) {
        final JanitorObject globals = env.getTemplateGlobals();
        final Scope belowObject = globals == null ? parent : of(globals, parent);
        return object == null ? belowObject : of(object, belowObject);
    }

    /**
     * @return the object whose attributes this scope offers
     */
    public @NotNull JanitorObject getObject() {
        return object;
    }

    @Override
    public @Nullable Scope getParent() {
        return parent;
    }

    @Override
    public @Nullable JanitorObject get(final @NotNull JanitorScriptProcess process, final @NotNull String name) {
        try {
            return object.janitorGetAttribute(process, name, false);
        } catch (JanitorRuntimeException e) {
            process.warn("Failed to get attribute " + name + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public void assign(final @NotNull JanitorScriptProcess process, final @NotNull String name, final @NotNull JanitorObject value) {
        process.getMainScope().bind(process, name, value);
    }

    @Override
    public @NotNull @Unmodifiable Collection<String> names() {
        if (object instanceof HasDispatcher<?> hasDispatcher) {
            return hasDispatcher.getDispatcher().streamAttributeNames().distinct().toList();
        }
        return List.of();
    }

    @Override
    public @Nullable NameDoc getNameDoc(final @NotNull String name) {
        return null;
    }

    @Override
    public @NotNull String janitorToString() {
        return "scope of " + object.janitorToString();
    }

}
