package com.eischet.janitor.maven.mojo;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Named
@Singleton
public class Lifecycle extends AbstractMavenLifecycleParticipant {

    public static final List<ScriptCallback> BUILD_SUCCESS_CALLBACKS = new ArrayList<>();
    public static final List<ScriptCallback> BUILD_FAILURE_CALLBACKS = new ArrayList<>();

    @Override
    public void afterSessionEnd(final MavenSession session) throws MavenExecutionException {
        boolean success = !session.getResult().hasExceptions();
        final JanitorRuntime runtime = MavenScriptingEnv.INSTANCE.newRuntime();
        try {
            if (success) {
                for (final ScriptCallback callback : BUILD_SUCCESS_CALLBACKS) {
                    runtime.executeCallback(callback.getScope(), callback.getCallable(), List.of());
                }
            } else {
                for (final ScriptCallback callback : BUILD_FAILURE_CALLBACKS) {
                    runtime.executeCallback(callback.getScope(), callback.getCallable(), List.of());
                }
            }
        } catch (JanitorRuntimeException e) {
            throw new MavenExecutionException("Error executing build callbacks", e);
        }
    }
}
