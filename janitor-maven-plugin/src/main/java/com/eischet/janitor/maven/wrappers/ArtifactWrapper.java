// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.jetbrains.annotations.NotNull;

/** Makes a Maven artifact available to scripts. */
public class ArtifactWrapper extends JanitorWrapper<Artifact> {

    private static final WrapperDispatchTable<Artifact> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("id", self -> self.janitorGetHostValue().getId())
                .setMetaData(Janitor.MetaData.HELP, "id: The full id of the artifact, made of group id, artifact id, type, classifier and version; read-only.");
        dispatcher.addStringProperty("dependencyConflictId", self -> self.janitorGetHostValue().getDependencyConflictId())
                .setMetaData(Janitor.MetaData.HELP, "dependencyConflictId: The version-less id of the artifact that Maven uses to detect conflicting dependencies; read-only.");
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId(), (self, value) -> self.janitorGetHostValue().setGroupId(value))
                .setMetaData(Janitor.MetaData.HELP, "groupId: The group id of the artifact; writable.");
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId(), (self, value) -> self.janitorGetHostValue().setArtifactId(value))
                .setMetaData(Janitor.MetaData.HELP, "artifactId: The artifact id of the artifact; writable.");
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value))
                .setMetaData(Janitor.MetaData.HELP, "version: The version of the artifact; writable.");
        dispatcher.addStringProperty("baseVersion", self -> self.janitorGetHostValue().getBaseVersion(), (self, value) -> self.janitorGetHostValue().setBaseVersion(value))
                .setMetaData(Janitor.MetaData.HELP, "baseVersion: The base version of the artifact, e.g. \"1.0-SNAPSHOT\" for a timestamped snapshot version; writable.");
        dispatcher.addStringProperty("scope", self -> self.janitorGetHostValue().getScope(), (self, value) -> self.janitorGetHostValue().setScope(value))
                .setMetaData(Janitor.MetaData.HELP, "scope: The dependency scope of the artifact, e.g. \"compile\" or \"test\"; writable.");
        dispatcher.addStringProperty("classifier", self -> self.janitorGetHostValue().getClassifier())
                .setMetaData(Janitor.MetaData.HELP, "classifier: The classifier of the artifact, e.g. \"sources\"; read-only.");
        dispatcher.addStringProperty("filename", self -> self.janitorGetHostValue().getFile().getPath())
                .setMetaData(Janitor.MetaData.HELP, "filename: The path of the artifact's file; read-only.");
        dispatcher.addStringProperty("absoluteFilename", self -> self.janitorGetHostValue().getFile().getAbsolutePath())
                .setMetaData(Janitor.MetaData.HELP, "absoluteFilename: The absolute path of the artifact's file; read-only.");
        dispatcher.addStringProperty("downloadUrl", self -> self.janitorGetHostValue().getDownloadUrl(), (self, value) -> self.janitorGetHostValue().setDownloadUrl(value))
                .setMetaData(Janitor.MetaData.HELP, "downloadUrl: The URL the artifact is downloaded from; writable.");
        dispatcher.addStringProperty("type", self -> self.janitorGetHostValue().getType())
                .setMetaData(Janitor.MetaData.HELP, "type: The type of the artifact, e.g. \"jar\" or \"pom\"; read-only.");
        dispatcher.addMethod("hasClassifier", (self, process, args) -> Janitor.toBool(self.janitorGetHostValue().hasClassifier()))
                .setMetaData(Janitor.MetaData.HELP, "hasClassifier(): Returns true if the artifact has a classifier.");
        dispatcher.addListProperty("dependencyTrail", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getDependencyTrail().stream().map(it -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().nullableString(it))))
                .setMetaData(Janitor.MetaData.HELP, "dependencyTrail: The ids of the artifacts through which this artifact was pulled in, as a list of strings; read-only.");
        dispatcher.addBooleanProperty("snapshot", self -> self.janitorGetHostValue().isSnapshot())
                .setMetaData(Janitor.MetaData.HELP, "snapshot: True if the artifact has a snapshot version; read-only.");
        dispatcher.addBooleanProperty("release", self -> self.janitorGetHostValue().isRelease(), (self, value) -> self.janitorGetHostValue().setRelease(value))
                .setMetaData(Janitor.MetaData.HELP, "release: True if the artifact is a release version; writable.");
        dispatcher.addBooleanProperty("resolved", self -> self.janitorGetHostValue().isResolved(), (self, value) -> self.janitorGetHostValue().setResolved(value))
                .setMetaData(Janitor.MetaData.HELP, "resolved: True if the artifact has been resolved; writable.");
        dispatcher.addBooleanProperty("optional", self -> self.janitorGetHostValue().isOptional(), (self, value) -> self.janitorGetHostValue().setOptional(value))
                .setMetaData(Janitor.MetaData.HELP, "optional: True if the artifact is an optional dependency; writable.");
        dispatcher.addMethod("isSelectedVersionKnown", (self, process, args) -> {
            try {
                return Janitor.toBool(self.janitorGetHostValue().isSelectedVersionKnown());
            } catch (OverConstrainedVersionException e) {
                throw new JanitorNativeException(process, "error checking if selected version is known", e);
            }
        })
                .setMetaData(Janitor.MetaData.HELP, "isSelectedVersionKnown(): Returns true if the version selected for the artifact is known; fails with an error if the version range is over-constrained.");
        dispatcher.addVoidMethod("setResolvedVersion", (self, process, args) -> {
            final String version = args.getRequiredStringValue(0);
            self.janitorGetHostValue().setResolvedVersion(version);
        })
                .setMetaData(Janitor.MetaData.HELP, "setResolvedVersion(version): Sets the resolved version of the artifact.");
    }

    public ArtifactWrapper(final @NotNull Artifact wrapped) {
        super(dispatcher, wrapped);
    }

    /**
     * @param wrapped the artifact
     * @return a wrapper for scripts
     */
    public static ArtifactWrapper of(final @NotNull Artifact wrapped) {
        return new ArtifactWrapper(wrapped);
    }

    /*

    LATER: add more properties/methods after writing matching wrappers

    void addMetadata(ArtifactMetadata var1);
    Collection<ArtifactMetadata> getMetadataList();
    void setRepository(ArtifactRepository var1);
    ArtifactRepository getRepository();
    void updateVersion(String var1, ArtifactRepository var2);
    ArtifactFilter getDependencyFilter();
    void setDependencyFilter(ArtifactFilter var1);
    ArtifactHandler getArtifactHandler();
    void setDependencyTrail(List<String> var1);
    VersionRange getVersionRange();
    void setVersionRange(VersionRange var1);
    void setArtifactHandler(ArtifactHandler var1);
    List<ArtifactVersion> getAvailableVersions();
    void setAvailableVersions(List<ArtifactVersion> var1);
    ArtifactVersion getSelectedVersion() throws OverConstrainedVersionException;

     */

}
