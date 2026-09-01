package com.eischet.janitor.repl;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleReplIO implements ReplIO {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public String readLine(String prompt) throws IOException {
        System.out.print(prompt);
        return reader.readLine();
    }

    @Override
    public void print(String text) {
        System.out.print(text);
    }

    @Override
    public void println(String text) {
        System.out.println(text);
    }

    @Override
    public void error(final String text) {
        System.err.println(text);
    }

    @Override
    public void exception(final Exception e) {
        System.out.println("Error: " + e.getMessage());
        // A JanitorRuntimeException's message is already a formatted script-level traceback
        // (module/line/source line down to the error), so the Java stack trace under it is just
        // interpreter-internal noise -- except for JanitorNativeException, where the cause is a
        // real Java exception from host code and the stack trace is the only way to debug it.

        // Janitor Runtime Exceptions are supposed to show a full script traceback within the message.
        // Therefore, we should NOT print the Java stack trace for them.
        if (e instanceof JanitorRuntimeException) {
            return;
        }

        // Other exceptions should print the Java stack trace.
        e.printStackTrace(System.err);
    }

}