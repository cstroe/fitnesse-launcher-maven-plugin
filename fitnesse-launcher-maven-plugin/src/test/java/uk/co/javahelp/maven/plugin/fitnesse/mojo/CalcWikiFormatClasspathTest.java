package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class CalcWikiFormatClasspathTest {

	private AbstractMojo mojo;
	
	private ArtifactHandler artifactHandler;
	
    private ArtifactResolver artifactResolver;
    
    private Artifact pluginArtifact;
    
    private Artifact fitnesseArtifact;
    
    private Plugin plugin;
    
    private ByteArrayOutputStream logStream;
	
	@Before
	public void setUp() {
		artifactHandler = mock(ArtifactHandler.class);
		artifactResolver = mock(ArtifactResolver.class);
		
        this.pluginArtifact = new DefaultArtifact(
        	getClass().getPackage().getName(), getClass().getSimpleName(), "version", "scope", "type", "classifier", artifactHandler);
        
        this.fitnesseArtifact = new DefaultArtifact(
            "org.fitnesse", "fitnesse", "20111025", "compile", "jar", null, artifactHandler);
        this.fitnesseArtifact.setFile(new File(getClass().getResource("dummy.jar").getPath()));
        
        this.plugin = new Plugin();
		plugin.setGroupId(pluginArtifact.getGroupId());
		plugin.setArtifactId(pluginArtifact.getArtifactId());
		
        Build build = new Build();
        build.addPlugin(plugin);
        
		mojo = new AbstractMojo() {
			@Override
			protected void executeInternal() { }
		};
		mojo.project = new MavenProject();
		mojo.resolver = this.artifactResolver;
		mojo.fitNesseHelper = mock(FitNesseHelper.class);
		
		mojo.pluginDescriptor = new PluginDescriptor();
		mojo.pluginDescriptor.setGroupId(pluginArtifact.getGroupId());
		mojo.pluginDescriptor.setArtifactId(pluginArtifact.getArtifactId());
		mojo.project.setPluginArtifacts(Collections.singleton(this.pluginArtifact));
		mojo.project.setBuild(build);
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
	}
	
	@Test
	public void testNoDependenciesNoFitNesseArtifact() {
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("[WARNING] Lookup for artifact [org.fitnesse:fitnesse] failed\n", logStream.toString());
	}
	
	@Test
	public void testNoDependenciesNoFitNesseJarFile() {
		
		mojo.pluginDescriptor.setArtifacts(Collections.singletonList(this.fitnesseArtifact));
        this.fitnesseArtifact.setFile(null);
		
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(Collections.singleton(this.fitnesseArtifact));
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class))).thenReturn(result);
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("[WARNING] File for artifact [org.fitnesse:fitnesse:jar:20111025:compile] is not found\n", logStream.toString());
	}
	
	@Test
	public void testNoDependenciesFitNesseOk() {
		
		mojo.pluginDescriptor.setArtifacts(Collections.singletonList(this.fitnesseArtifact));
		
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(Collections.singleton(this.fitnesseArtifact));
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class))).thenReturn(result);
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("", logStream.toString());
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
	}
	
	@Test
	public void testOneDependencyOneArtifact() {
		
		List<Artifact> artifacts = Arrays.asList(
            this.fitnesseArtifact,
			createArtifact("g1", "a1")
		);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(artifacts.get(1))));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("", logStream.toString());
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(artifacts.get(1)));
	}
	
	private Dependency createDependecy(String groupId, String artifactId) {
		Dependency dependency = new Dependency();
		dependency.setGroupId(groupId);
		dependency.setArtifactId(artifactId);
		return dependency;
	}
	
	private Artifact createArtifact(String groupId, String artifactId) {
        Artifact artifact = new DefaultArtifact(
            groupId, artifactId, "1.0.0", "compile", "jar", null, artifactHandler);
        artifact.setFile(new File(getClass().getResource("dummy.jar").getPath()));
        return artifact;
	}
	
	private ArtifactResolutionResult createArtifactResolutionResult(Set<Artifact> artifacts) {
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(artifacts);
		return result;
	}
}
