package com.eischet.janitor.repl;

import java.io.IOException;

public interface ReplIO {
    String readLine(String prompt) throws IOException;
    void print(String text);
    void println(String text);
    void error(String text);
    default void verbose(String text) { }
    void exception(Exception e);
}
