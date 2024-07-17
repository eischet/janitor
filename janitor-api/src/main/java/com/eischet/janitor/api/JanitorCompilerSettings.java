package com.eischet.janitor.api;

/**
 * Settings for the Janitor compiler.
 * TODO: move these to the implementation package instead.
 */
public class JanitorCompilerSettings {

    private boolean verbose;
    private boolean relaxNullPointers;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isRelaxNullPointers() {
        return relaxNullPointers;
    }

    public void setRelaxNullPointers(final boolean relaxNullPointers) {
        this.relaxNullPointers = relaxNullPointers;
    }

    public static final JanitorCompilerSettings DEFAUlTS;
    public static final JanitorCompilerSettings RELAXED;

    static {
        DEFAUlTS = new JanitorCompilerSettings();
        DEFAUlTS.setVerbose(false);
        DEFAUlTS.setRelaxNullPointers(false);

        RELAXED = new JanitorCompilerSettings();
        RELAXED.setVerbose(false);
        RELAXED.setRelaxNullPointers(true);
    }

}
