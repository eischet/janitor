package com.eischet.janitor.api.errors.glue;

import com.eischet.janitor.api.errors.JanitorException;

/**
 * Control Flow Exceptions are used internally by the Janitor interpreter.
 * These exceptions should never be thrown to outside users, unless there is a bug in the interpreter.
 */
public abstract class JanitorControlFlowException extends JanitorException {
}
