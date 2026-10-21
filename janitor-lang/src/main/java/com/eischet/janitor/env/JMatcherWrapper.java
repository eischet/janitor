// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JStringBase;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;

import java.util.regex.Matcher;

/** Wraps a Java regular expression {@link Matcher} for use in scripts. */
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
        })
                .setMetaData(Janitor.MetaData.HELP, "start(group): Meant to return the start index of the current match (of a group). Currently unusable: there is no find() to select a match, and passing a group fails.");
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
        })
                .setMetaData(Janitor.MetaData.HELP, "end(group): Meant to return the end index of the current match (of a group). Currently unusable, like start().");
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
        })
                .setMetaData(Janitor.MetaData.HELP, "group(group): Meant to return the text of the current match (of a group). Currently unusable, like start().");

    }

    public JMatcherWrapper(final Matcher matcher) {
        super(DISPATCH, matcher);
    }
}
