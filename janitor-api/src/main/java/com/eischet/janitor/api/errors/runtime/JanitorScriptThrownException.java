// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exceptions thrown by scripts, which are not originally thrown as Exception instances in the script.
 * <p>As in early Python, we can throw arbitrary objects, and these come wrapped as JanitorScriptThrownException instances.
 * Note that having the option of throwing anything does not necessarily mean it's a good idea to do so.</p>
 * <p>If you want to supply an Exception constructor to a script, use JanitorRuntimeException subclasses instead.
 * This class has been made 'final' to remind you of this.</p>
 */
public final class JanitorScriptThrownException extends JanitorRuntimeException implements JanitorObject {
    private final @NotNull JanitorObject scriptMessage;

    public JanitorScriptThrownException(final @NotNull JanitorScriptProcess process, final @NotNull JanitorObject message) {
        super(process, message.janitorToString(), null, JanitorScriptThrownException.class);
        this.scriptMessage = message;
    }

    /**
     * @return the value that the script threw
     */
    public @NotNull JanitorObject getScriptMessage() {
        return scriptMessage;
    }

    /**
     * In a catch block, {@code e.value} is the thrown value, and all attributes that errors do not have themselves are read from it,
     * so {@code throw {code: 404};} can be caught and read as {@code e.code}. The attributes of errors ({@code message},
     * {@code traceback}, {@code type}) take precedence; {@code e.value.message} reads a key "message" of a thrown map.
     */
    @Override
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorRuntimeException {
        return switch (name) {
            case "value" -> scriptMessage;
            case "message", "traceback", "type" -> super.janitorGetAttribute(process, name, required);
            default -> scriptMessage.janitorGetAttribute(process, name, required);
        };
    }
}
