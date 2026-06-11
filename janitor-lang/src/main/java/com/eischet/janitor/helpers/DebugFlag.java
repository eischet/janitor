package com.eischet.janitor.helpers;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.logging.Debuggable;
import com.eischet.janitor.toolbox.memory.Flag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A DebugFlag is a flag that can be used to enable or disable debug mode for a specific entity,
 * and that can be controlled by scripts when needed.
 */
public class DebugFlag extends JanitorComposed<DebugFlag> implements Debuggable {

    public static final DispatchTable<DebugFlag> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH
            .setMetaData(Janitor.MetaData.HELP, "A flag that can be used to enable or disable debug mode for a specific entity, and that can be controlled by scripts when needed.");
        DISPATCH
            .addBooleanProperty("active", self -> self.flag.isChecked(), (self, checked) -> self.flag.setFlag(checked))
            .setMetaData(Janitor.MetaData.HELP, "true when enabled, false when not enabled.");
        DISPATCH
            .addStringProperty("name", self -> self.name)
            .setMetaData(Janitor.MetaData.HELP, "The internal name of the flag. Read-only.");
    }

    protected final @NotNull Flag flag;
    protected final @NotNull String name;

    public DebugFlag(final @NotNull String name) {
        super(DISPATCH);
        this.flag = new Flag();
        this.name = name;
    }

    @Override
    public boolean isDebugModeEnabled() {
        return flag.isChecked();
    }

    @Override
    public @Nullable String getDebugEntityName() {
        return name;
    }
}
