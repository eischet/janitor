package com.eischet.janitor.maven.mojo;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.lang.JNativeMethod;
import com.eischet.janitor.maven.env.MavenScriptingEnv;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
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
import org.eclipse.aether.resolution.ArtifactResult;
import org.jetbrains.annotations.NotNull;
import org.apache.maven.plugins.annotations.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.resolution.DependencyResult;

@Mojo(name = "run-script-file", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RunScriptMojo extends AbstractMojo implements Lookups {

    /**
     * The Maven Session Object; copied this from the Maven Assembly Mojo
     * Might need later for all the fun stuff in the session!
     */
    //@Parameter(defaultValue = "${session}", readonly = true, required = true)
    //private MavenSession mavenSession;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "scriptFile", required = false)
    private File scriptFile;

    @Parameter(property = "script", required = false)
    private String script;


    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Component
    private RepositorySystem repoSystem;


    public RepositorySystem lookupRepositorySystem() {
        return repoSystem;
    }


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
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
            });

            if (result != JNull.NULL) {
                getLog().error(result.janitorToString());
                throw new MojoFailureException(result.janitorToString());
            }

        } catch (JanitorCompilerException e) {
            throw new MojoExecutionException("Error compiling script", e);
        } catch (JanitorRuntimeException e) {
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
        // I'm not sure why this differs so much from project.artifacts, but this seems to work "better" for me.
        // Create a collect request for the project's dependencies
        CollectRequest collectRequest = new CollectRequest();
        project.getDependencies().forEach(dependency -> {
            org.eclipse.aether.artifact.Artifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
                    dependency.getClassifier(),
                    dependency.getType(),
                    dependency.getVersion()
            );
            collectRequest.addDependency(new Dependency(artifact, JavaScopes.COMPILE));
        });
        collectRequest.setRepositories(project.getRemoteProjectRepositories());
        // Resolve dependencies
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
        DependencyResult dependencyResult = repoSystem.resolveDependencies(repoSession, dependencyRequest);
        return MavenScriptingEnv.INSTANCE.getBuiltinTypes().list(
                dependencyResult.getArtifactResults().stream()
                        .map(ArtifactResult::getArtifact)
                        .map(AetherArtifactWrapper::of)
        );
    }
}
