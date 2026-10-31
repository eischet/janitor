// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.apache.maven.model.Dependency;
import org.jetbrains.annotations.NotNull;

/** Makes a Maven dependency available to scripts. */
public class DependencyWrapper extends JanitorWrapper<Dependency> {

    private static final WrapperDispatchTable<Dependency> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId(), (self, value) -> self.janitorGetHostValue().setArtifactId(value))
                .setMetaData(Janitor.MetaData.HELP, "artifactId: The artifact id of the dependency; writable.");
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId(), (self, value) -> self.janitorGetHostValue().setGroupId(value))
                .setMetaData(Janitor.MetaData.HELP, "groupId: The group id of the dependency; writable.");
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value))
                .setMetaData(Janitor.MetaData.HELP, "version: The version of the dependency; writable.");
        dispatcher.addStringProperty("scope", self -> self.janitorGetHostValue().getScope(), (self, value) -> self.janitorGetHostValue().setScope(value))
                .setMetaData(Janitor.MetaData.HELP, "scope: The scope of the dependency, e.g. \"compile\" or \"test\"; writable.");
        dispatcher.addStringProperty("type", self -> self.janitorGetHostValue().getType(), (self, value) -> self.janitorGetHostValue().setType(value))
                .setMetaData(Janitor.MetaData.HELP, "type: The type of the dependency, e.g. \"jar\" or \"pom\"; writable.");
        dispatcher.addStringProperty("classifier", self -> self.janitorGetHostValue().getClassifier(), (self, value) -> self.janitorGetHostValue().setClassifier(value))
                .setMetaData(Janitor.MetaData.HELP, "classifier: The classifier of the dependency, e.g. \"sources\"; writable.");
        dispatcher.addStringProperty("systemPath", self -> self.janitorGetHostValue().getSystemPath(), (self, value) -> self.janitorGetHostValue().setSystemPath(value))
                .setMetaData(Janitor.MetaData.HELP, "systemPath: The path of the file for a dependency with scope \"system\"; writable.");
        dispatcher.addStringProperty("optional", self -> self.janitorGetHostValue().getOptional(), (self, value) -> self.janitorGetHostValue().setOptional(value))
                .setMetaData(Janitor.MetaData.HELP, "optional: The optional flag of the dependency as a string, e.g. \"true\"; writable.");
    }

    public DependencyWrapper(final @NotNull Dependency wrapped) {
        super(dispatcher, wrapped);
    }

}
