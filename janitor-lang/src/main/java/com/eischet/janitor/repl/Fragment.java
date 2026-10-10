// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.repl;

import com.eischet.janitor.lang.JanitorParser;

class Fragment {

    private final PartialParseResult parseResult;
    private final JanitorParser.ScriptContext scriptContext;
    private final boolean missingStatementTerminator;

    public Fragment(final PartialParseResult partialParseResult) {
        this.parseResult = partialParseResult;
        this.scriptContext = null;
        this.missingStatementTerminator = false;
    }

    public Fragment(final JanitorParser.ScriptContext scriptContext) {
        this.parseResult = PartialParseResult.OK;
        this.scriptContext = scriptContext;
        this.missingStatementTerminator = false;
    }

    public Fragment(final boolean missingStatementTerminator) {
        this.parseResult = null;
        this.scriptContext = null;
        this.missingStatementTerminator = missingStatementTerminator;
    }

    /**
     * @return whether the parsed text was complete, or null if the parse result is unknown
     */
    public PartialParseResult getParseResult() {
        return parseResult;
    }

    /**
     * @return the parse tree, or null if the text could not be parsed
     */
    public JanitorParser.ScriptContext getScriptContext() {
        return scriptContext;
    }

    /**
     * @return true if parsing failed because the final statement terminator (semicolon) is missing
     */
    public boolean isMissingStatementTerminator() {
        return missingStatementTerminator;
    }
}
