// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/** Makes a Maven project available to scripts. */
public class MavenProjectWrapper extends JanitorWrapper<MavenProject> {

    private static final WrapperDispatchTable<MavenProject> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId(), (self, value) -> self.janitorGetHostValue().setArtifactId(value))
                .setMetaData(Janitor.MetaData.HELP, "artifactId: The artifact id of the project; writable.");
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId(), (self, value) -> self.janitorGetHostValue().setGroupId(value))
                .setMetaData(Janitor.MetaData.HELP, "groupId: The group id of the project; writable.");
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value))
                .setMetaData(Janitor.MetaData.HELP, "version: The version of the project; writable.");
        dispatcher.addStringProperty("packaging", self -> self.janitorGetHostValue().getPackaging(), (self, value) -> self.janitorGetHostValue().setPackaging(value))
                .setMetaData(Janitor.MetaData.HELP, "packaging: The packaging of the project, e.g. \"jar\" or \"pom\"; writable.");
        dispatcher.addStringProperty("modelVersion", self -> self.janitorGetHostValue().getModelVersion(), (self, value) -> self.janitorGetHostValue().setModelVersion(value))
                .setMetaData(Janitor.MetaData.HELP, "modelVersion: The POM model version of the project, e.g. \"4.0.0\"; writable.");
        dispatcher.addObjectProperty("model", self -> new ModelWrapper(self.janitorGetHostValue().getModel()))
                .setMetaData(Janitor.MetaData.HELP, "model: The project's model, i.e. the contents of its POM; read-only.");
        dispatcher.addBuilderMethod("addCompileSourceRoot", (self, process, args) -> self.janitorGetHostValue().addCompileSourceRoot(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addCompileSourceRoot(path): Adds a directory of sources to compile to the project; returns the project.");
        dispatcher.addObjectProperty("build", self -> new MavenBuildWrapper(self.janitorGetHostValue().getBuild()))
                .setMetaData(Janitor.MetaData.HELP, "build: The build section of the project; read-only.");
        dispatcher.addObjectProperty("artifact", self -> new ArtifactWrapper(self.janitorGetHostValue().getArtifact()))
                .setMetaData(Janitor.MetaData.HELP, "artifact: The artifact that the project produces; read-only.");

        dispatcher.addListProperty("artifacts", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getArtifacts().stream().map(ArtifactWrapper::of)))
                .setMetaData(Janitor.MetaData.HELP, "artifacts: The resolved dependency artifacts of the project, as a list; read-only.");
        dispatcher.addListProperty("pluginArtifacts", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getPluginArtifacts().stream().map(ArtifactWrapper::of)))
                .setMetaData(Janitor.MetaData.HELP, "pluginArtifacts: The artifacts of the project's build plugins, as a list; read-only.");
        dispatcher.addListProperty("attachedArtifacts", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getAttachedArtifacts().stream().map(ArtifactWrapper::of)))
                .setMetaData(Janitor.MetaData.HELP, "attachedArtifacts: The additional artifacts attached to the project, e.g. sources or javadoc jars, as a list; read-only.");

        dispatcher.addObjectProperty("parent", self -> {
            final MavenProject parent = self.janitorGetHostValue().getParent();
            if (parent == null) {
                return JNull.NULL;
            }
            return new MavenProjectWrapper(parent);
        })
                .setMetaData(Janitor.MetaData.HELP, "parent: The parent project, or null if there is none; read-only.");

        dispatcher.addStringProperty("basedir", self -> {
            final File basedir = self.janitorGetHostValue().getBasedir();
            if (basedir == null) {
                return null;
            }
            return basedir.getAbsolutePath();
        })
                .setMetaData(Janitor.MetaData.HELP, "basedir: The absolute path of the project's base directory, or null if there is none; read-only.");
        // LATER: dispatcher.addObjectProperty("properties", ); cannot automatically wrap this at the moment

        dispatcher.addStringProperty("revision", self -> self.janitorGetHostValue().getProperties().getProperty("revision"))
                .setMetaData(Janitor.MetaData.HELP, "revision: The value of the project property \"revision\", or null if it is not set; read-only.");

        dispatcher.addMethod("getProperty", (self, process, args) -> {
            final String name = args.getRequiredStringValue(0);
            final String value = self.janitorGetHostValue().getProperties().getProperty(name);
            return process.getBuiltins().nullableString(value);
        })
                .setMetaData(Janitor.MetaData.HELP, "getProperty(name): Returns the value of the project property with the given name, or null if it is not set.");
        dispatcher.addVoidMethod("setProperty", (self, process, args) -> {
            // LATER: we can set props just fine, but this does not get reflected inside the maven project. The maven properties plugin mentions this, and
            // there does not seem to be a workaround. Leaving this here for future updates, even though it does not do anything noticeable.
            final String name = args.getRequiredStringValue(0);
            final String value = args.getRequiredStringValue(1);
            self.janitorGetHostValue().getProperties().setProperty(name, value);
        })
                .setMetaData(Janitor.MetaData.HELP, "setProperty(name, value): Sets a project property; getProperty sees the new value, but the rest of the Maven build does not.");
    }

    public MavenProjectWrapper(final @NotNull MavenProject wrapped) {
        super(dispatcher, wrapped);
    }

    /** Placeholder for experiments with the names of the Maven model's properties; it does nothing. */
    public void testNames() {
        // wrapped.modelgetPackaging()
    }
}
