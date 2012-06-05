package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException;
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
        this.fitnesseArtifact.setFile(new File(getClass().getResource("/dummy.jar").getPath()));
        
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
		assertEquals(String.format("[WARNING] Lookup for artifact [org.fitnesse:fitnesse] failed%n"), logStream.toString());
	}
	
	@Test
	public void testNoDependenciesNoFitNesseJarFile() {
		
		mojo.pluginDescriptor.setArtifacts(Collections.singletonList(this.fitnesseArtifact));
        this.fitnesseArtifact.setFile(null);
		
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(Collections.singleton(this.fitnesseArtifact));
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class))).thenReturn(result);
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals(String.format("[WARNING] File for artifact [org.fitnesse:fitnesse:jar:20111025:compile] is not found%n"), logStream.toString());
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
		
        Artifact g1a1 = createArtifact("g1", "a1");
		List<Artifact> artifacts = Arrays.asList(this.fitnesseArtifact, g1a1);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(g1a1)));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("", logStream.toString());
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
	}
	
	@Test
	public void testOneDependencyTwoArtifacts() {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
		List<Artifact> artifacts = Arrays.asList(this.fitnesseArtifact, g1a1);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a2)));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("", logStream.toString());
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
	}
	
	@Test
	public void testTwoDependenciesTwoArtifacts() {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g2a1 = createArtifact("g2", "a1");
		List<Artifact> artifacts = Arrays.asList(this.fitnesseArtifact, g1a1, g2a1);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		this.plugin.addDependency(createDependecy("g2","a1"));
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(g1a1)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(g2a1)));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("", logStream.toString());
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
	}
	
	@Test
	public void testMultiDependenciesManyArtifactsEach() {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(this.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		this.plugin.addDependency(createDependecy("g2","a1"));
		this.plugin.addDependency(createDependecy("g3","a3"));
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a2, g1a3)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a2, g3a3, g3a4, g3a5)));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("", logStream.toString());
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	}
	
	@Test
	public void testWithoutDependecyAddedToPluginArtifactsAreNotResolved() {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(this.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		this.plugin.addDependency(createDependecy("g2","a1"));
        // g3 is not added as a dependency
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a2, g1a3)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a2, g3a3, g3a4, g3a5)));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals("", logStream.toString());
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	}
	
	@Test
	public void testMissingArtifacts() {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(this.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		this.plugin.addDependency(createDependecy("g2","a1"));
		this.plugin.addDependency(createDependecy("g3","a3"));
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a3), Arrays.asList(g1a2)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a3, g3a4), Arrays.asList(g3a2, g3a5)));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		assertEquals(String.format(
		    "[WARNING] Could not resolve artifact: [g1:a2:jar:1.0.0:compile]%n" +
        	"[WARNING] Could not resolve artifact: [g3:a2:jar:1.0.0:compile]%n" +
			"[WARNING] Could not resolve artifact: [g3:a5:jar:1.0.0:compile]%n"), logStream.toString());

		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	}
	
	@Test
	public void testArtifactResolutionExceptions() {
		
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_DEBUG, "test", new PrintStream(logStream))));
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(this.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		this.plugin.addDependency(createDependecy("g1","a1"));
		this.plugin.addDependency(createDependecy("g2","a1"));
		this.plugin.addDependency(createDependecy("g3","a3"));
		mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(this.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a3), new ArtifactResolutionException("TEST", g1a2)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a3, g3a4),
	    		new MultipleArtifactsNotFoundException(g3a3, Arrays.asList(g3a1, g3a4), Arrays.asList(g3a2, g3a5), null)));
		
		assertEquals("\n", mojo.calcWikiFormatClasspath());
		//System.out.print(logStream.toString());
		assertTrue(logStream.toString().contains(String.format("org.apache.maven.artifact.resolver.ArtifactResolutionException: TEST%n  g1:a2:jar:1.0.0")));
		assertTrue(logStream.toString().contains(String.format("org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException: Missing:\n----------\n1) g3:a2:jar:1.0.0%n%n")));
		assertTrue(logStream.toString().contains(String.format("1) g3:a2:jar:1.0.0%n%n  Try downloading the file manually from the project website.")));
		assertTrue(logStream.toString().contains(String.format("2) g3:a5:jar:1.0.0%n%n  Try downloading the file manually from the project website.")));
		assertTrue(logStream.toString().contains(String.format("\n----------\n2 required artifacts are missing.\n\nfor artifact: %n  g3:a3:jar:1.0.0")));
		
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(fitnesseArtifact));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
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
        artifact.setFile(new File(getClass().getResource("/dummy.jar").getPath()));
        return artifact;
	}
	
	private ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts) {
	    return createArtifactResolutionResult(artifacts, null, null);
	}
	
	private ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts, List<Artifact> missingArtifacts) {
	    return createArtifactResolutionResult(artifacts, missingArtifacts, null);
	}
	
	private ArtifactResolutionResult createArtifactResolutionResult(Collection<Artifact> artifacts, ArtifactResolutionException exception) {
	    return createArtifactResolutionResult(artifacts, null, exception);
	}
	
	private ArtifactResolutionResult createArtifactResolutionResult(
			Collection<Artifact> artifacts, List<Artifact> missingArtifacts, ArtifactResolutionException exception) {
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(new HashSet<Artifact>(artifacts));
		if(missingArtifacts != null) {
			result.setUnresolvedArtifacts(missingArtifacts);
		}
		if(exception != null) {
			result.addErrorArtifactException(exception);
		}
		return result;
	}
}
