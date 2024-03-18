package com.eischet.janitor.api.types;

import com.eischet.janitor.runtime.types.JanitorClass;
import org.eclipse.collections.api.factory.Maps;
import org.jetbrains.annotations.NotNull;

public class JString2 implements JanitorInstance<JString2> {

    private static final JString2Class myClass = new JString2Class();
    public JString2() {

    }

    @Override
    public @NotNull JanitorClass<JString2> getJanitorClass() {
        return myClass;
    }

    public static class JString2Class extends JanitorClass<JString2> {
        public JString2Class() {
            super(null, Maps.immutable.empty());
        }
    }

}
