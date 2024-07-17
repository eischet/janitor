package com.eischet.janitor.toolbox.memory;

import com.eischet.janitor.toolbox.listeners.ListenerSet;
import com.eischet.janitor.toolbox.listeners.ListenerSetStandard;

public class Flag {

    private final ListenerSet<FlagListener> listeners = new ListenerSetStandard<>();
    private transient boolean flag;

    public boolean isFlag() {
        return flag;
    }

    public Flag setFlag(final boolean flag) {
        this.flag = flag;
        listeners.stream().forEach(listener -> listener.onFlagChanged(flag));
        return this;
    }

    public boolean invert() {
        setFlag(!isFlag());
        return isFlag();
    }

    public Flag addListener(final FlagListener listener) {
        listeners.add(listener);
        return this;
    }

    @FunctionalInterface
    public interface FlagListener {
        void onFlagChanged(boolean flag);
    }

}
