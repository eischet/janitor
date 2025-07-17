package com.eischet.janitor.repl;

public interface ReplIO {
    String readLine(String prompt) throws Exception;
    void print(String text);
    void println(String text);
    void error(String text);
    default void verbose(String text) { }
}
