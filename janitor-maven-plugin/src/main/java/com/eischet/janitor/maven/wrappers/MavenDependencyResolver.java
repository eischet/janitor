package com.eischet.janitor.maven.wrappers;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.artifact.JavaScopes;

import java.util.Arrays;
import java.util.List;

public class MavenDependencyResolver {

    public static void main(String[] args) {
        // Initialize repository system and session
        RepositorySystem system = Booter.newRepositorySystem();
        RepositorySystemSession session = Booter.newRepositorySystemSession(system);

        // Define the artifact (groupId:artifactId:version)
        DefaultArtifact artifact = new DefaultArtifact("com.example:my-app:1.0");

        // Create a collect request
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(newRepositories());

        // Create a dependency request
        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);

        try {
            // Resolve dependencies
            DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

            // Print resolved artifacts
            for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
                System.out.println("Resolved: " + artifactResult.getArtifact().getFile());
            }
        } catch (DependencyResolutionException e) {
            e.printStackTrace();
        }
    }

    private static List<RemoteRepository> newRepositories() {
        return Arrays.asList(
                new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build()
        );
    }
}