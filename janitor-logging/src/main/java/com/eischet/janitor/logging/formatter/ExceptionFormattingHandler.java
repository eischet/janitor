package com.eischet.janitor.logging.formatter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Formats exceptions of certain types for log output. */
public interface ExceptionFormattingHandler {

    @Nullable String formatLogException(@NotNull Throwable throwable);

}
