package com.eischet.janitor.maven.mojo;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.lang.JNativeMethod;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.maven.env.MavenScriptingEnvProvider;
import com.eischet.janitor.maven.wrappers.AetherArtifactWrapper;
import com.eischet.janitor.maven.wrappers.ModelWrapper;
import com.eischet.janitor.maven.wrappers.MavenProjectWrapper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Mojo(name = "run-script-file", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RunScriptMojo extends AbstractMojo {

    /**
     * The Maven Session Object; copied this from the Maven Assembly Mojo
     * Might need later for all the fun stuff in the session!
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession mavenSession;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "scriptFile")
    private File scriptFile;

    @Parameter(property = "script")
    private String script;

    @Parameter(property = "skip")
    private boolean skip;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    private RepositorySystem repoSystem;

    @Inject
    public void setRepoSystem(final RepositorySystem repoSystem) {
        this.repoSystem = repoSystem;
    }


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution");
            return;
        }
        Janitor.setUserProvider(new MavenScriptingEnvProvider()); // Workaround: the @AutoService should theoretically make this unnecessary, but does not work at the moment!?
        final String contents = pickContents(script, scriptFile);
        if (script != null && !script.isBlank()) {
            getLog().info("Running inline script");
        } else {
            getLog().info("Running script: " + scriptFile.getAbsolutePath());
        }
        try {
            final JanitorRuntime rt = MavenScriptingEnv.INSTANCE.newRuntime();
            final RunnableScript script = rt.compile(scriptFile == null ? "inline-script" : scriptFile.getName(), contents);
            final @NotNull JanitorObject result = script.run(g -> {
                g.bind("project", new MavenProjectWrapper(project));
                g.bind("model", new ModelWrapper(project.getModel()));
                g.bind("getDependencies", new JNativeMethod((process, arguments) -> resolveDependencies()));
                g.bind("extractManifestClassPath", new JNativeMethod((process, arguments) -> {
                    return Janitor.list(extractManifestClassPath(arguments.getRequiredStringValue(0)).stream().map(Janitor::string));
                }));
            });

            if (result != JNull.NULL) {
                getLog().error(result.janitorToString());
                throw new MojoFailureException(result.janitorToString());
            }

        } catch (JanitorCompilerException e) {
            throw new MojoExecutionException("Error compiling script", e);
        } catch (JanitorRuntimeException | RuntimeException e) {
            throw new MojoExecutionException("Error running script", e);
        }

    }

    private String pickContents(final String script, final File scriptFile) throws MojoExecutionException {
        if (scriptFile == null && script == null) {
            throw new MojoExecutionException("Either script or scriptFile is required");
        }
        if (scriptFile != null && (script != null && !script.isBlank())) {
            throw new MojoExecutionException("Only one of script or scriptFile is allowed! scriptFile=" + scriptFile.getAbsolutePath() + " script=" + script);
        }
        if (script != null) {
            return script;
        } else {
            try {
                return Files.readString(scriptFile.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new MojoExecutionException("Error reading script file", e);
            }
        }
    }

    private JList resolveDependencies() throws DependencyResolutionException {
        // Create a collect request for the project's dependencies
        // This will resolve ALL transitive dependencies with proper version mediation
        CollectRequest collectRequest = new CollectRequest();

        // Add the project's direct dependencies as roots
        project.getDependencies().forEach(dependency -> {
            org.eclipse.aether.artifact.Artifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
                    dependency.getClassifier(),
                    dependency.getType(),
                    dependency.getVersion()
            );
            collectRequest.addDependency(new Dependency(artifact, dependency.getScope()));
        });

        // Add managed dependencies for proper version resolution
        // This ensures transitive dependencies get the correct managed versions
        if (project.getDependencyManagement() != null && project.getDependencyManagement().getDependencies() != null) {
            project.getDependencyManagement().getDependencies().forEach(dependency -> {
                org.eclipse.aether.artifact.Artifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(
                        dependency.getGroupId(),
                        dependency.getArtifactId(),
                        dependency.getClassifier(),
                        dependency.getType(),
                        dependency.getVersion()
                );
                collectRequest.addManagedDependency(new Dependency(artifact, dependency.getScope()));
            });
        }

        collectRequest.setRepositories(project.getRemoteProjectRepositories());

        // Resolve dependencies (this includes all transitives with proper version mediation)
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
        DependencyResult dependencyResult = repoSystem.resolveDependencies(repoSession, dependencyRequest);

        return Janitor.list(
                dependencyResult.getArtifactResults().stream()
                        .map(ArtifactResult::getArtifact)
                        .map(AetherArtifactWrapper::of)
        );
    }

    /**
     * Extracts the JAR file names from the Class-Path attribute in a JAR's MANIFEST.MF
     *
     * @param jarFileName the path to the JAR file
     * @return list of JAR file names referenced in the manifest Class-Path
     * @throws IOException if the JAR file cannot be read or has no manifest
     */
    private List<String> extractManifestClassPath(String jarFileName) throws IOException {
        try (JarFile jarFile = new JarFile(jarFileName)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return new ArrayList<>();
            }

            String classPath = manifest.getMainAttributes().getValue("Class-Path");
            if (classPath == null || classPath.isBlank()) {
                return new ArrayList<>();
            }

            // Class-Path entries are space-separated
            return Arrays.asList(classPath.trim().split("\\s+"));
        }
    }
}
