package com.eischet.janitor.repl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleReplIO implements ReplIO {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public String readLine(String prompt) throws Exception {
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

}