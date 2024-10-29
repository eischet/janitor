package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.apache.maven.model.Build;

public class MavenBuildWrapper extends JanitorWrapper<Build> {

    private static final WrapperDispatchTable<Build> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("directory", self -> self.janitorGetHostValue().getDirectory(), (self, value) -> self.janitorGetHostValue().setDirectory(value));
        dispatcher.addStringProperty("outputDirectory", self -> self.janitorGetHostValue().getOutputDirectory(), (self, value) -> self.janitorGetHostValue().setOutputDirectory(value));
        dispatcher.addStringProperty("finalName", self -> self.janitorGetHostValue().getFinalName(), (self, value) -> self.janitorGetHostValue().setFinalName(value));
        dispatcher.addStringProperty("testOutputDirectory", self -> self.janitorGetHostValue().getTestOutputDirectory(), (self, value) -> self.janitorGetHostValue().setTestOutputDirectory(value));
        dispatcher.addStringProperty("sourceDirectory", self -> self.janitorGetHostValue().getSourceDirectory(), (self, value) -> self.janitorGetHostValue().setSourceDirectory(value));
        dispatcher.addStringProperty("testSourceDirectory", self -> self.janitorGetHostValue().getTestSourceDirectory(), (self, value) -> self.janitorGetHostValue().setTestSourceDirectory(value));
        dispatcher.addStringProperty("scriptSourceDirectory", self -> self.janitorGetHostValue().getScriptSourceDirectory(), (self, value) -> self.janitorGetHostValue().setScriptSourceDirectory(value));
        dispatcher.addStringProperty("defaultGoal", self -> self.janitorGetHostValue().getDefaultGoal(), (self, value) -> self.janitorGetHostValue().setDefaultGoal(value));
    }


    public MavenBuildWrapper(final Build build) {
        super(dispatcher, build);
    }
}
