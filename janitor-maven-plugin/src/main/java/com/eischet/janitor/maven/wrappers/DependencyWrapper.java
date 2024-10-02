package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.apache.maven.model.Dependency;
import org.jetbrains.annotations.NotNull;

public class DependencyWrapper extends JanitorWrapper<Dependency> {

    private static final WrapperDispatchTable<Dependency> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId(), (self, value) -> self.janitorGetHostValue().setArtifactId(value));
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId(), (self, value) -> self.janitorGetHostValue().setGroupId(value));
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value));
        dispatcher.addStringProperty("scope", self -> self.janitorGetHostValue().getScope(), (self, value) -> self.janitorGetHostValue().setScope(value));
        dispatcher.addStringProperty("type", self -> self.janitorGetHostValue().getType(), (self, value) -> self.janitorGetHostValue().setType(value));
        dispatcher.addStringProperty("classifier", self -> self.janitorGetHostValue().getClassifier(), (self, value) -> self.janitorGetHostValue().setClassifier(value));
        dispatcher.addStringProperty("systemPath", self -> self.janitorGetHostValue().getSystemPath(), (self, value) -> self.janitorGetHostValue().setSystemPath(value));
        dispatcher.addStringProperty("optional", self -> self.janitorGetHostValue().getOptional(), (self, value) -> self.janitorGetHostValue().setOptional(value));
    }

    public DependencyWrapper(final @NotNull Dependency wrapped) {
        super(dispatcher, wrapped);
    }

}
