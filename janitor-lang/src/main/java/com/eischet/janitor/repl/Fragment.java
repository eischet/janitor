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

    public PartialParseResult getParseResult() {
        return parseResult;
    }

    public JanitorParser.ScriptContext getScriptContext() {
        return scriptContext;
    }

    public boolean isMissingStatementTerminator() {
        return missingStatementTerminator;
    }
}
