package com.eischet.janitor.helpers;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.logging.Debuggable;
import com.eischet.janitor.toolbox.memory.Flag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A DebugFlag is a flag that can be used to enable or disable debug mode for a specific entity,
 * and that can be controlled by scripts when needed. Used to selectively enable/disable verbose
 * logging for whole subsystems or for selected entities by combining multiple flags via and/or.
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

    /*
     * Create a named Debug Flag.
     * The flag is off initially.
     */
    public DebugFlag(final @NotNull String name) {
        super(DISPATCH);
        this.flag = new Flag();
        this.name = name;
    }

    /**
     * Activate the flag.
     * @return this
     */
    public DebugFlag activate() {
        flag.setFlag(true);
        return this;
    }

    /**
     * Deactivate the flag.
     * @return this
     */
    public DebugFlag deactivate() {
        flag.setFlag(false);
        return this;
    }

    @Override
    public boolean isDebugModeEnabled() {
        return flag.isChecked();
    }

    @Override
    public @Nullable String getDebugEntityName() {
        return name;
    }

    /**
     * Combine a number of debuggables via logical OR.
     * @param debuggables any amount of debuggables
     * @return an or-ed combination
     */
    public static Debuggable or(final Debuggable... debuggables) {
        return new OrCombiner(debuggables);
    }

    /**
     * Combine a number of debuggables via logical AND.
     * @param debuggables any amount of debuggables
     * @return an and-ed combination
     */
    public static Debuggable and(final Debuggable... debuggables) {
        return new AndCombiner(debuggables);
    }

    /**
     * Negate a Debuggable
     * @param debuggable the Debuggable to negate.
     * @return the inverse of the Debuggable
     */
    public static Debuggable not(final Debuggable debuggable) {
        final String name = "!" + debuggable.getDebugEntityName();
        return new Debuggable() {
            @Override
            public boolean isDebugModeEnabled() {
                return !debuggable.isDebugModeEnabled();
            }

            @Override
            public @NotNull String getDebugEntityName() {
                return name;
            }
        };
    }

    /**
     * Combine a number of debuggables, leaving the actual combination operation to subclasses.
     */
    private static abstract class Combiner implements Debuggable {
        protected final String name;
        protected final List<Debuggable> debuggables;

        /**
         * Create a new combiner.
         * @param debuggables any amount of debuggables
         */
        public Combiner(final Debuggable[] debuggables) {
            this.debuggables = Arrays.asList(debuggables);
            this.name = Arrays.stream(debuggables).map(Debuggable::getDebugEntityName).filter(Objects::nonNull).collect(Collectors.joining("|"));
        }

        @Override
        public @Nullable String getDebugEntityName() {
            return name;
        }
    }

    /**
     * Combine a number of debuggables via logical OR.
     */
    private static class OrCombiner extends Combiner {
        /**
         * Create a new OR combiner.
         * @param debuggables any amount of debuggables
         */
        public OrCombiner(final Debuggable[] debuggables) {
            super(debuggables);
        }

        @Override
        public boolean isDebugModeEnabled() {
            return debuggables.stream().anyMatch(Debuggable::isDebugModeEnabled);
        }
    }

    /**
     * Combine a number of debuggables via logical AND.
     */
    private static class AndCombiner extends Combiner {
        /**
         * Create a new AND combiner.
         * @param debuggables any amount of  debuggables
         */
        public AndCombiner(final Debuggable[] debuggables) {
            super(debuggables);
        }

        @Override
        public boolean isDebugModeEnabled() {
            return debuggables.stream().allMatch(Debuggable::isDebugModeEnabled);
        }
    }

}
