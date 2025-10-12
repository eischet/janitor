package com.eischet.janitor.logging;

import java.io.OutputStream;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class JanitorLoggingConsoleHandler extends StreamHandler {
    public JanitorLoggingConsoleHandler(final OutputStream out, final Formatter formatter, final ErrorManager errorManager) {
        super(out, formatter);
        setErrorManager(errorManager);
    }

    @Override
    public synchronized void publish(final LogRecord record) {
        super.publish(record);
        flush();
    }

    @Override
    public synchronized void flush() {
        super.flush();
    }
}
