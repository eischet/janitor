package com.eischet.janitor;


import com.eischet.janitor.maven.mojo.RunScriptMojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class MyMojoTest {
    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething() throws Exception {
        File pom = new File("target/test-classes/project-to-test/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        RunScriptMojo myMojo = (RunScriptMojo) rule.lookupConfiguredMojo(pom, "run-script-file");
        assertNotNull(myMojo);
        myMojo.execute();

        File scriptFile = (File) rule.getVariableValueFromObject(myMojo, "scriptFile");
        assertNotNull(scriptFile);
        assertTrue(scriptFile.exists());


        /*
        File outputDirectory = (File) rule.getVariableValueFromObject(myMojo, "outputDirectory");
        assertNotNull(outputDirectory);
        assertTrue(outputDirectory.exists());
         */
        /*
        File touch = new File(outputDirectory, "touch.txt");
        assertTrue(touch.exists());

        File expectedOutputDirectory = new File(pom.getAbsoluteFile(), "target/test-harness/project-to-test");
        assertEquals(expectedOutputDirectory, outputDirectory);

         */
    }

    /**
     * Do not need the MojoRule.
     */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
        assertTrue(true);
    }

}

