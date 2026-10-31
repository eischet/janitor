// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/** Makes a Maven Aether artifact available to scripts. */
public class AetherArtifactWrapper extends JanitorWrapper<Artifact> {
    private static final WrapperDispatchTable<Artifact> dispatcher = new WrapperDispatchTable<>(null);

    static {
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId())
                .setMetaData(Janitor.MetaData.HELP, "groupId: The group id of the artifact; read-only.");
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId())
                .setMetaData(Janitor.MetaData.HELP, "artifactId: The artifact id of the artifact; read-only.");
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> replace(self, self.janitorGetHostValue().setVersion(value)))
                .setMetaData(Janitor.MetaData.HELP, "version: The version of the artifact; writable.");
        dispatcher.addStringProperty("baseVersion", self -> self.janitorGetHostValue().getBaseVersion())
                .setMetaData(Janitor.MetaData.HELP, "baseVersion: The base version of the artifact, e.g. \"1.0-SNAPSHOT\" for a timestamped snapshot version; read-only.");
        dispatcher.addBooleanProperty("snapshot", self -> self.janitorGetHostValue().isSnapshot())
                .setMetaData(Janitor.MetaData.HELP, "snapshot: True if the artifact has a snapshot version; read-only.");
        dispatcher.addStringProperty("classifier", self -> self.janitorGetHostValue().getClassifier())
                .setMetaData(Janitor.MetaData.HELP, "classifier: The classifier of the artifact, e.g. \"sources\"; read-only.");
        dispatcher.addStringProperty("extension", self -> self.janitorGetHostValue().getExtension())
                .setMetaData(Janitor.MetaData.HELP, "extension: The file extension of the artifact, e.g. \"jar\"; read-only.");
        dispatcher.addStringProperty("filename", self -> self.janitorGetHostValue().getFile().getPath())
                .setMetaData(Janitor.MetaData.HELP, "filename: The path of the artifact's file; read-only.");
        dispatcher.addStringProperty("absoluteFilename", self -> self.janitorGetHostValue().getFile().getPath())
                .setMetaData(Janitor.MetaData.HELP, "absoluteFilename: The path of the artifact's file, currently the same as filename; read-only.");
        dispatcher.addMethod("getProperty", ((self, process, arguments) -> {
            final String key = arguments.getRequiredStringValue(0);
            final String value = self.janitorGetHostValue().getProperties().get(key);
            return process.getBuiltins().nullableString(value);
        }))
                .setMetaData(Janitor.MetaData.HELP, "getProperty(key): Returns the value of the artifact property with the given key, or null if it is not set.");
        dispatcher.addVoidMethod("setProperty", ((self, process, arguments) -> {
            final String key = arguments.getRequiredStringValue(0);
            final String value = arguments.getRequiredStringValue(1);
            final Map<String, String> properties = new HashMap<>(self.janitorGetHostValue().getProperties());
            properties.put(key, value);
            replace(self, self.janitorGetHostValue().setProperties(properties));
        }))
                .setMetaData(Janitor.MetaData.HELP, "setProperty(key, value): Sets the artifact property with the given key.");
    }

    /**
     * Aether artifacts are immutable: their setters return a changed copy, which the wrapper keeps from then on.
     */
    private static void replace(final JanitorWrapper<Artifact> self, final Artifact changed) {
        ((AetherArtifactWrapper) self).wrapped = changed;
    }

    public AetherArtifactWrapper(final @NotNull Artifact wrapped) {
        super(dispatcher, wrapped);
    }

    /**
     * @param wrapped the artifact
     * @return a wrapper for scripts
     */
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
