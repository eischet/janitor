package com.eischet.janitor.modules.brrr;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;

import java.util.List;

public class BrrrModule extends JanitorComposed<BrrrModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("brrr", BrrrModule::new);
    public static final DispatchTable<BrrrModule> DISPATCH = new DispatchTable<>(BrrrModule::new);

    static {
        DISPATCH.addMethod("send", (self, process, args) -> {
            if (args.size() == 2) {
                return self.sendMessage(process, args.getRequiredStringValue(0), args.get(1));
            } else if (args.size() == 1) {
                return self.sendMessage(process, null, args.get(0));
            } else {
                throw new JanitorArgumentException(process, "send requires at least 1 argument: send([url], message)");
            }
        });
        DISPATCH.addMethod("Message", (self, process, args) -> {
            final BrrrMessage message = new BrrrMessage();
            if (args.size() > 0) {
                final JMap map = args.getRequired(0, JMap.class);
                map.applyTo(process, message);
            }
            return message;
        });
    }

    public JanitorObject sendMessage(final JanitorScriptProcess process, final String url, final JanitorObject messageArg) throws JanitorRuntimeException {
        final JCallArgs sendArgs = url == null ? JCallArgs.empty("send", process) : new JCallArgs("send", process, List.of(Janitor.string(url)));
        if (messageArg instanceof BrrrMessage msg) {
            return msg.send(process, sendArgs);
        }
        if (messageArg instanceof JMap map) {
            final BrrrMessage message = new BrrrMessage();
            map.applyTo(process, message);
            return message.send(process, sendArgs);
        }
        if (messageArg instanceof JString str) {
            final BrrrMessage message = new BrrrMessage();
            message.setMessage(str.janitorGetHostValue());
            return message.send(process, sendArgs);
        }
        throw new JanitorArgumentException(process, "send requires a message argument of type BrrrMessage, Map or String");
    }


    public BrrrModule() {
        super(DISPATCH);
    }

}
