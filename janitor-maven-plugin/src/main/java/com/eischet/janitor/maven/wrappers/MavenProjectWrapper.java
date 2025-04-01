package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

public class MavenProjectWrapper extends JanitorWrapper<MavenProject> {

    private static final WrapperDispatchTable<MavenProject> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId(), (self, value) -> self.janitorGetHostValue().setArtifactId(value));
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId(), (self, value) -> self.janitorGetHostValue().setGroupId(value));
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value));
        dispatcher.addStringProperty("packaging", self -> self.janitorGetHostValue().getPackaging(), (self, value) -> self.janitorGetHostValue().setPackaging(value));
        dispatcher.addStringProperty("modelVersion", self -> self.janitorGetHostValue().getModelVersion(), (self, value) -> self.janitorGetHostValue().setModelVersion(value));
        dispatcher.addObjectProperty("model", self -> new ModelWrapper(self.janitorGetHostValue().getModel()));
        dispatcher.addBuilderMethod("addCompileSourceRoot", (self, process, args) -> self.janitorGetHostValue().addCompileSourceRoot(args.getRequiredStringValue(0)));
        dispatcher.addObjectProperty("build", self -> new MavenBuildWrapper(self.janitorGetHostValue().getBuild()));
        dispatcher.addObjectProperty("artifact", self -> new ArtifactWrapper(self.janitorGetHostValue().getArtifact()));

        dispatcher.addListProperty("artifacts", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getArtifacts().stream().map(ArtifactWrapper::of)));
        dispatcher.addListProperty("pluginArtifacts", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getPluginArtifacts().stream().map(ArtifactWrapper::of)));
        dispatcher.addListProperty("attachedArtifacts", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getAttachedArtifacts().stream().map(ArtifactWrapper::of)));

        dispatcher.addObjectProperty("parent", self -> {
            final MavenProject parent = self.janitorGetHostValue().getParent();
            if (parent == null) {
                return JNull.NULL;
            }
            return new MavenProjectWrapper(parent);
        });

        dispatcher.addStringProperty("basedir", self -> self.janitorGetHostValue().getBasedir().getAbsolutePath());
        // LATER: dispatcher.addObjectProperty("properties", ); cannot automatically wrap this at the moment

        dispatcher.addMethod("getProperty", (self, process, args) -> {
            final String name = args.getRequiredStringValue(0);
            final String value = self.janitorGetHostValue().getProperties().getProperty(name);
            return process.getBuiltins().nullableString(value);
        });
        dispatcher.addVoidMethod("setProperty", (self, process, args) -> {
            // LATER: we can set props just fine, but this does not get reflected inside the maven project. The maven properties plugin mentions this, and
            // there does not seem to be a workaround. Leaving this here for future updates, even though it does not do anything noticeable.
            final String name = args.getRequiredStringValue(0);
            final String value = args.getRequiredStringValue(1);
            self.janitorGetHostValue().getProperties().setProperty(name, value);
        });
    }

    public MavenProjectWrapper(final @NotNull MavenProject wrapped) {
        super(dispatcher, wrapped);
    }

    public void testNames() {
        // wrapped.modelgetPackaging()
    }
}
