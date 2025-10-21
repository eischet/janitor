package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

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

    public @NotNull JanitorObject getScriptMessage() {
        return scriptMessage;
    }
}
