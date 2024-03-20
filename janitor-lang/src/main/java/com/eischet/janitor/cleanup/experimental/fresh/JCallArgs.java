package com.eischet.janitor.cleanup.experimental.fresh;

import org.jetbrains.annotations.Nullable;

public interface JCallArgs {

    int size();
    @Nullable JObject getArgumentValue(int index);
    @Nullable JClass getArgumentClass(int index);

    JCallArgs NONE = new JCallArgs() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public @Nullable JObject getArgumentValue(final int index) {
            return null;
        }

        @Override
        public @Nullable JClass getArgumentClass(final int index) {
            return null;
        }
    };

}
