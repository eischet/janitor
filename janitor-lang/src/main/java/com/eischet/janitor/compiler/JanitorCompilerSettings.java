package com.eischet.janitor.compiler;

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

    public static JanitorCompilerSettings DEFAUlTS;
    public static JanitorCompilerSettings RELAXED;

    static {
        DEFAUlTS = new JanitorCompilerSettings();
        DEFAUlTS.setVerbose(false);
        DEFAUlTS.setRelaxNullPointers(false);

        RELAXED = new JanitorCompilerSettings();
        RELAXED.setVerbose(false);
        RELAXED.setRelaxNullPointers(true);
    }

}
