package com.eischet.janitor.maven.wrappers;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import org.apache.maven.model.*;

public class ModelWrapper extends JanitorWrapper<Model> {

    private static final WrapperDispatchTable<Model> dispatcher = new WrapperDispatchTable<>();

    static {
        dispatcher.setConstructor((process, args) -> ModelWrapper.construct(process, args));
        dispatcher.addStringProperty("modelVersion", self -> self.janitorGetHostValue().getModelVersion(), (self, value) -> self.janitorGetHostValue().setModelVersion(value));
        dispatcher.addStringProperty("groupId", self -> self.janitorGetHostValue().getGroupId(), (self, value) -> self.janitorGetHostValue().setGroupId(value));
        dispatcher.addStringProperty("artifactId", self -> self.janitorGetHostValue().getArtifactId(), (self, value) -> self.janitorGetHostValue().setArtifactId(value));
        dispatcher.addStringProperty("version", self -> self.janitorGetHostValue().getVersion(), (self, value) -> self.janitorGetHostValue().setVersion(value));
        dispatcher.addStringProperty("packaging", self -> self.janitorGetHostValue().getPackaging(), (self, value) -> self.janitorGetHostValue().setPackaging(value));
        dispatcher.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value));
        dispatcher.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value));
        dispatcher.addStringProperty("url", self -> self.janitorGetHostValue().getUrl(), (self, value) -> self.janitorGetHostValue().setUrl(value));
        dispatcher.addStringProperty("inceptionYear", self -> self.janitorGetHostValue().getInceptionYear(), (self, value) -> self.janitorGetHostValue().setInceptionYear(value));
        dispatcher.addStringProperty("modelEncoding", self -> self.janitorGetHostValue().getModelEncoding(), (self, value) -> self.janitorGetHostValue().setModelEncoding(value));
        dispatcher.addListProperty("dependencies", self -> MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(self.janitorGetHostValue().getDependencies().stream().map(DependencyWrapper::new)));
        dispatcher.addBuilderMethod("addDependency", (self, process, args) -> {
            /* TODO: get a dependency from args
            final Dependency dependency = new Dependency();
            self.janitorGetHostValue().addDependency(dependency);
            final JanitorDependencyWrapper w = new JanitorDependencyWrapper(dependency);
             */
        });

        // MODEL:
        // private Parent parent;
        // private String childProjectUrlInheritAppendPath;
        // private Organization organization;
        // private List<License> licenses;
        // private List<Developer> developers;
        // private List<Contributor> contributors;
        // private List<MailingList> mailingLists;
        // private Prerequisites prerequisites;
        // private Scm scm;
        // private IssueManagement issueManagement;
        // private CiManagement ciManagement;
        // private Build build;
        // private List<Profile> profiles;
        // private File pomFile;

        // MODEL BASE:
        // private List<String> modules;
        // private DistributionManagement distributionManagement;
        // private Properties properties;
        // private DependencyManagement dependencyManagement;
        // private List<Dependency> dependencies;
        // private List<Repository> repositories;
        // private List<Repository> pluginRepositories;
        // private Object reports;
        // private Reporting reporting;
        // private Map<Object, InputLocation> locations;
        // private InputLocation location;
        // private InputLocation modulesLocation;
        // private InputLocation distributionManagementLocation;
        // private InputLocation propertiesLocation;
        // private InputLocation dependencyManagementLocation;
        // private InputLocation dependenciesLocation;
        // private InputLocation repositoriesLocation;
        // private InputLocation pluginRepositoriesLocation;
        // private InputLocation reportsLocation;
        // private InputLocation reportingLocation;

    }

    public static ModelWrapper construct(final JanitorScriptProcess process, final JCallArgs args) {
        return new ModelWrapper(new Model()); // TODO: .applyConstructorArgs(args)
    }

    public ModelWrapper(final Model model) {
        super(dispatcher, model);
    }
}
