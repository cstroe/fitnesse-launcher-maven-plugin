package uk.co.javahelp.maven.plugin.fitnesse;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;

public class TestArtifactFactory {
	
    public static Artifact fitNesseArtifact(ArtifactHandler artifactHandler) {
        return new DefaultArtifact(
        		"org.fitnesse", "fitnesse", "20140201", "compile", "jar", null, artifactHandler);
    }
}
