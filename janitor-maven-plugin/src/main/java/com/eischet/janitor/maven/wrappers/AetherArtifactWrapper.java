package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

public class AetherArtifactWrapper extends JanitorWrapper<Artifact> {
    private static final WrapperDispatchTable<Artifact> dispatcher = new WrapperDispatchTable<>(null);

    static {
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId());
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId());
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value));
        dispatcher.addStringProperty("baseVersion", self -> self.janitorGetHostValue().getBaseVersion());
        dispatcher.addBooleanProperty("snapshot", self -> self.janitorGetHostValue().isSnapshot());
        dispatcher.addStringProperty("classifier", self -> self.janitorGetHostValue().getClassifier());
        dispatcher.addStringProperty("extension", self -> self.janitorGetHostValue().getExtension());
        dispatcher.addStringProperty("filename", self -> self.janitorGetHostValue().getFile().getPath());
        dispatcher.addStringProperty("absoluteFilename", self -> self.janitorGetHostValue().getFile().getPath());
        dispatcher.addMethod("getProperty", ((self, process, arguments) -> {
            final String key = arguments.getRequiredStringValue(0);
            final String value = self.janitorGetHostValue().getProperties().get(key);
            return process.getBuiltins().nullableString(value);
        }));
        dispatcher.addVoidMethod("setProperty", ((self, process, arguments) -> {
            final String key = arguments.getRequiredStringValue(0);
            final String value = arguments.getRequiredStringValue(1);
            self.janitorGetHostValue().getProperties().put(key, value);
        }));
    }

    public AetherArtifactWrapper(final @NotNull Artifact wrapped) {
        super(dispatcher, wrapped);
    }

    public static AetherArtifactWrapper of(final @NotNull Artifact wrapped) {
        return new AetherArtifactWrapper(wrapped);
    }

    @Override
    public @NotNull String janitorToString() {
        final String claf = janitorGetHostValue().getClassifier();
        if (claf != null && !claf.isBlank()) {
            return String.format("%s:%s:%s:%s", janitorGetHostValue().getGroupId(), janitorGetHostValue().getArtifactId(), janitorGetHostValue().getVersion(), janitorGetHostValue().getClassifier());
        } else {
            return String.format("%s:%s:%s", janitorGetHostValue().getGroupId(), janitorGetHostValue().getArtifactId(), janitorGetHostValue().getVersion());
        }
    }
}
