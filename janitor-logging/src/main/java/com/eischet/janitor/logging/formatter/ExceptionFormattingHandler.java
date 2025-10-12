package com.eischet.janitor.logging.formatter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExceptionFormattingHandler {

    @Nullable String formatLogException(@NotNull Throwable throwable);

}
