package com.eischet.janitor.env;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JStringBase;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;

import java.util.regex.Matcher;

public class JMatcherWrapper extends JanitorWrapper<Matcher> {

    private static final WrapperDispatchTable<Matcher> DISPATCH = new WrapperDispatchTable<>(null);

    static {
        DISPATCH.addMethod("start", (self, process, arguments) -> {
            arguments.require(0, 1);
            if (arguments.size() == 0) {
                return process.getBuiltins().integer(self.janitorGetHostValue().start());
            } else if (arguments.size() == 1) {
                final JanitorObject groupArg = arguments.get(1);
                if (groupArg instanceof JStringBase stringBase) {
                    return process.getBuiltins().integer(self.janitorGetHostValue().start(stringBase.toString()));
                } else {
                    final int group = arguments.getRequiredIntValue(1);
                    return process.getBuiltins().integer(self.janitorGetHostValue().start(group));
                }
            }
            return JNull.NULL;
        });
        DISPATCH.addMethod("end", (self, process, arguments) -> {
            arguments.require(0, 1);
            if (arguments.size() == 0) {
                return process.getBuiltins().integer(self.janitorGetHostValue().end());
            } else if (arguments.size() == 1) {
                final JanitorObject groupArg = arguments.get(1);
                if (groupArg instanceof JStringBase stringBase) {
                    return process.getBuiltins().integer(self.janitorGetHostValue().end(stringBase.toString()));
                } else {
                    final int group = arguments.getRequiredIntValue(1);
                    return process.getBuiltins().integer(self.janitorGetHostValue().end(group));
                }
            }
            return JNull.NULL;
        });
        DISPATCH.addMethod("group", (self, process, arguments) -> {
            arguments.require(0, 1);
            if (arguments.size() == 0) {
                return process.getBuiltins().string(self.janitorGetHostValue().group());
            } else if (arguments.size() == 1) {
                final JanitorObject groupArg = arguments.get(1);
                if (groupArg instanceof JStringBase stringBase) {
                    return process.getBuiltins().string(self.janitorGetHostValue().group(stringBase.toString()));
                } else {
                    final int group = arguments.getRequiredIntValue(1);
                    return process.getBuiltins().string(self.janitorGetHostValue().group(group));
                }
            }
            return JNull.NULL;
        });

    }

    public JMatcherWrapper(final Matcher matcher) {
        super(DISPATCH, matcher);
    }
}
