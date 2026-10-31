// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.apache.maven.model.Build;

/** Makes the build section of a Maven project available to scripts. */
public class MavenBuildWrapper extends JanitorWrapper<Build> {

    private static final WrapperDispatchTable<Build> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("directory", self -> self.janitorGetHostValue().getDirectory(), (self, value) -> self.janitorGetHostValue().setDirectory(value))
                .setMetaData(Janitor.MetaData.HELP, "directory: The build directory, usually \"target\"; writable.");
        dispatcher.addStringProperty("outputDirectory", self -> self.janitorGetHostValue().getOutputDirectory(), (self, value) -> self.janitorGetHostValue().setOutputDirectory(value))
                .setMetaData(Janitor.MetaData.HELP, "outputDirectory: The directory that compiled classes are written to; writable.");
        dispatcher.addStringProperty("finalName", self -> self.janitorGetHostValue().getFinalName(), (self, value) -> self.janitorGetHostValue().setFinalName(value))
                .setMetaData(Janitor.MetaData.HELP, "finalName: The file name of the built artifact, without extension; writable.");
        dispatcher.addStringProperty("testOutputDirectory", self -> self.janitorGetHostValue().getTestOutputDirectory(), (self, value) -> self.janitorGetHostValue().setTestOutputDirectory(value))
                .setMetaData(Janitor.MetaData.HELP, "testOutputDirectory: The directory that compiled test classes are written to; writable.");
        dispatcher.addStringProperty("sourceDirectory", self -> self.janitorGetHostValue().getSourceDirectory(), (self, value) -> self.janitorGetHostValue().setSourceDirectory(value))
                .setMetaData(Janitor.MetaData.HELP, "sourceDirectory: The directory of the main Java sources; writable.");
        dispatcher.addStringProperty("testSourceDirectory", self -> self.janitorGetHostValue().getTestSourceDirectory(), (self, value) -> self.janitorGetHostValue().setTestSourceDirectory(value))
                .setMetaData(Janitor.MetaData.HELP, "testSourceDirectory: The directory of the test Java sources; writable.");
        dispatcher.addStringProperty("scriptSourceDirectory", self -> self.janitorGetHostValue().getScriptSourceDirectory(), (self, value) -> self.janitorGetHostValue().setScriptSourceDirectory(value))
                .setMetaData(Janitor.MetaData.HELP, "scriptSourceDirectory: The directory of the script sources; writable.");
        dispatcher.addStringProperty("defaultGoal", self -> self.janitorGetHostValue().getDefaultGoal(), (self, value) -> self.janitorGetHostValue().setDefaultGoal(value))
                .setMetaData(Janitor.MetaData.HELP, "defaultGoal: The goal or phase that is run when Maven is called without one; writable.");
    }


    public MavenBuildWrapper(final Build build) {
        super(dispatcher, build);
    }
}
