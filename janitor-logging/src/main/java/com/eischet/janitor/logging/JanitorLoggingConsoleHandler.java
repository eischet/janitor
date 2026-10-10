// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;

import java.io.OutputStream;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/** A console handler for java.util.logging that flushes after every log record, so that output appears immediately. */
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
