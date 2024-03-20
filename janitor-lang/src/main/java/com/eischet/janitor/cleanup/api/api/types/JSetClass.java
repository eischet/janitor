package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.runtime.types.JUnboundMethod;
import com.eischet.janitor.cleanup.runtime.types.JanitorClass;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public class JSetClass extends JanitorClass<JSet> {
    private static final ImmutableMap<String, JUnboundMethod<JSet>> methods;

    static {
        final MutableMap<String, JUnboundMethod<JSet>> m = Maps.mutable.empty();

        m.put("add", (self, runningScript, arguments) -> JBool.map(self.add(arguments.require(1).get(0))));
        m.put("remove", (self, runningScript, arguments) -> JBool.map(self.remove(arguments.require(1).get(0))));
        m.put("contains", (self, runningScript, arguments) -> JBool.map(self.contains(arguments.require(1).get(0))));
        m.put("toList", (self, runningScript, arguments) -> {
            arguments.require(0);
            return JList.of(self.set.stream());
        });
        m.put("size", (self, runningScript, arguments) -> {
            arguments.require(0);
            return JInt.of(self.set.size());
        });
        m.put("isEmpty", (self, runningScript, arguments) -> {
            arguments.require(0);
            return JBool.map(self.set.isEmpty());
        });

        methods = m.toImmutable();

    }

    public JSetClass() {
        super(null, methods);
    }

}
