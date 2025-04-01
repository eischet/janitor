package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.jetbrains.annotations.NotNull;

public class ArtifactWrapper extends JanitorWrapper<Artifact> {

    private static final WrapperDispatchTable<Artifact> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("id", self -> self.janitorGetHostValue().getId());
        dispatcher.addStringProperty("dependencyConflictId", self -> self.janitorGetHostValue().getDependencyConflictId());
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId(), (self, value) -> self.janitorGetHostValue().setGroupId(value));
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId(), (self, value) -> self.janitorGetHostValue().setArtifactId(value));
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value));
        dispatcher.addStringProperty("baseVersion", self -> self.janitorGetHostValue().getBaseVersion(), (self, value) -> self.janitorGetHostValue().setBaseVersion(value));
        dispatcher.addStringProperty("scope", self -> self.janitorGetHostValue().getScope(), (self, value) -> self.janitorGetHostValue().setScope(value));
        dispatcher.addStringProperty("classifier", self -> self.janitorGetHostValue().getClassifier());
        dispatcher.addStringProperty("filename", self -> self.janitorGetHostValue().getFile().getPath());
        dispatcher.addStringProperty("absoluteFilename", self -> self.janitorGetHostValue().getFile().getAbsolutePath());
        dispatcher.addStringProperty("downloadUrl", self -> self.janitorGetHostValue().getDownloadUrl(), (self, value) -> self.janitorGetHostValue().setDownloadUrl(value));
        dispatcher.addStringProperty("type", self -> self.janitorGetHostValue().getType());
        dispatcher.addMethod("hasClassifier", (self, process, args) -> JBool.of(self.janitorGetHostValue().hasClassifier()));
        dispatcher.addListProperty("dependencyTrail", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getDependencyTrail().stream().map(it -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().nullableString(it))));
        dispatcher.addBooleanProperty("snapshot", self -> self.janitorGetHostValue().isSnapshot());
        dispatcher.addBooleanProperty("release", self -> self.janitorGetHostValue().isRelease(), (self, value) -> self.janitorGetHostValue().setRelease(value));
        dispatcher.addBooleanProperty("resolved", self -> self.janitorGetHostValue().isResolved(), (self, value) -> self.janitorGetHostValue().setResolved(value));
        dispatcher.addBooleanProperty("optional", self -> self.janitorGetHostValue().isOptional(), (self, value) -> self.janitorGetHostValue().setOptional(value));
        dispatcher.addMethod("isSelectedVersionKnown", (self, process, args) -> {
            try {
                return JBool.of(self.janitorGetHostValue().isSelectedVersionKnown());
            } catch (OverConstrainedVersionException e) {
                throw new JanitorNativeException(process, "error checking if selected version is known", e);
            }
        });
        dispatcher.addVoidMethod("setResolvedVersion", (self, process, args) -> {
            final String version = args.getRequiredStringValue(0);
            self.janitorGetHostValue().setResolvedVersion(version);
        });
    }

    public ArtifactWrapper(final @NotNull Artifact wrapped) {
        super(dispatcher, wrapped);
    }

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
