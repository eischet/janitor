package com.eischet.janitor.maven.mojo;

import com.eischet.janitor.maven.env.MavenScriptingEnv;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.maven.wrappers.ModelWrapper;
import com.eischet.janitor.maven.wrappers.MavenProjectWrapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


@Mojo(name = "run-script-file", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RunScriptMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "scriptFile", required = false)
    private File scriptFile;

    @Parameter(property = "script", required = false)
    private String script;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Running script: " + scriptFile.getAbsolutePath());

        try {
            final JanitorRuntime rt = MavenScriptingEnv.INSTANCE.newRuntime();
            final String contents = pickContents(script, scriptFile);
            final RunnableScript script = rt.compile(scriptFile.getName(), contents);
            script.run(g -> {
                g.bind("project", new MavenProjectWrapper(project));
                g.bind("model", new ModelWrapper(project.getModel()));
            });
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
        if (scriptFile != null && script != null) {
            throw new MojoExecutionException("Only one of script or scriptFile is allowed");
        }
        if (script != null) {
            return script;
        } else {
            try {
                final String contents = Files.readString(scriptFile.toPath(), StandardCharsets.UTF_8);
                return contents;
            } catch (IOException e) {
                throw new MojoExecutionException("Error reading script file", e);
            }
        }
    }
}
