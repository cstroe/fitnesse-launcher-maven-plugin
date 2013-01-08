package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.logging.Logger;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class AbstractFitNesseMojoTestHelper {

	AbstractFitNesseMojo mojo;
	
	ArtifactHandler artifactHandler;
	
    ArtifactResolver artifactResolver;
    
    Artifact pluginArtifact;
    
    Artifact fitnesseArtifact;
    
    Plugin plugin;
	
    ClassRealm realm;
    
    ByteArrayOutputStream logStream;

    AbstractFitNesseMojoTestHelper() {
		artifactHandler = mock(ArtifactHandler.class);
		artifactResolver = mock(ArtifactResolver.class);
		realm = mock(ClassRealm.class);
		
        this.pluginArtifact = new DefaultArtifact(
        	getClass().getPackage().getName(), getClass().getSimpleName(), "version", "scope", "type", "classifier", artifactHandler);
        
        this.fitnesseArtifact = new DefaultArtifact(
            "org.fitnesse", "fitnesse", "20121220", "compile", "jar", null, artifactHandler);
        this.fitnesseArtifact.setFile(new File(getClass().getResource("/dummy.jar").getPath()));
        
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(Collections.singleton(this.fitnesseArtifact));
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class))).thenReturn(result);
        
        this.plugin = new Plugin();
		plugin.setGroupId(pluginArtifact.getGroupId());
		plugin.setArtifactId(pluginArtifact.getArtifactId());
		
        Build build = new Build();
        build.addPlugin(plugin);
        
		mojo = new AbstractFitNesseMojo() {
			@Override
			protected void executeInternal() { }
		};
		mojo.project = new MavenProject();
		mojo.resolver = this.artifactResolver;
		mojo.fitNesseHelper = mock(FitNesseHelper.class);
		
		mojo.pluginDescriptor = new PluginDescriptor();
		mojo.pluginDescriptor.setGroupId(pluginArtifact.getGroupId());
		mojo.pluginDescriptor.setArtifactId(pluginArtifact.getArtifactId());
		mojo.pluginDescriptor.setArtifacts(Collections.singletonList(this.fitnesseArtifact));
		mojo.pluginDescriptor.setClassRealm(realm);
		mojo.project.setPluginArtifacts(Collections.singleton(this.pluginArtifact));
		mojo.project.setBuild(build);
		mojo.project.setFile(new File(getClass().getResource("pom.xml").getPath()));
		mojo.project.setArtifactId("ARTIFACT_ID");
		mojo.project.setVersion("VERSION");
		mojo.project.getBuild().setTestOutputDirectory("target/test-classes");
		mojo.project.getBuild().setOutputDirectory("target/classes");
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
	}
	
}
