package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
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
    }

    public MavenProjectWrapper(final @NotNull MavenProject wrapped) {
        super(dispatcher, wrapped);

    }

    public void testNames() {
        // wrapped.modelgetPackaging()
    }
}
