package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException;
import org.apache.maven.model.Dependency;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.Logger;
import org.junit.Before;
import org.junit.Test;

public class CalcWikiFormatClasspathTest {

	private AbstractFitNesseMojoTestHelper helper;
	
	@Before
	public void setUp() {
		helper = new AbstractFitNesseMojoTestHelper();
	}
	
	@Test
	public void testNoDependenciesNoFitNesseArtifact() throws MojoExecutionException {
		helper.mojo.pluginDescriptor.setArtifacts(null);
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(String.format("[WARNING] Lookup for artifact [org.fitnesse:fitnesse] failed%n"), helper.logStream.toString());
	}
	
	@Test
	public void testNoDependenciesNoFitNesseJarFile() throws MojoExecutionException {
		
        helper.fitnesseArtifact.setFile(null);
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(String.format("[WARNING] File for artifact [org.fitnesse:fitnesse:jar:20121220:compile] is not found%n"), helper.logStream.toString());
	}
	
	@Test
	public void testNoDependenciesFitNesseOk() throws MojoExecutionException {
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals("", helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
	}
	
	@Test
	public void testOneDependencyOneArtifact() throws MojoExecutionException {
		
        Artifact g1a1 = createArtifact("g1", "a1");
		List<Artifact> artifacts = Arrays.asList(helper.fitnesseArtifact, g1a1);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(helper.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(g1a1)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals("", helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
	}
	
	@Test
	public void testOneDependencyTwoArtifacts() throws MojoExecutionException {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
		List<Artifact> artifacts = Arrays.asList(helper.fitnesseArtifact, g1a1);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(helper.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a2)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals("", helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
	}
	
	@Test
	public void testTwoDependenciesTwoArtifacts() throws MojoExecutionException {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g2a1 = createArtifact("g2", "a1");
		List<Artifact> artifacts = Arrays.asList(helper.fitnesseArtifact, g1a1, g2a1);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(helper.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(g1a1)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(g2a1)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals("", helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
	}
	
	@Test
	public void testMultiDependenciesManyArtifactsEach() throws MojoExecutionException {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.plugin.addDependency(createDependecy("g3","a3"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(helper.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a2, g1a3)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a2, g3a3, g3a4, g3a5)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals("", helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	}
	
	@Test
	public void testWithoutDependecyAddedToPluginArtifactsAreNotResolved() throws MojoExecutionException {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
        // g3 is not added as a dependency
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(helper.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a2, g1a3)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a2, g3a3, g3a4, g3a5)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals("", helper.logStream.toString());
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	}
	
	@Test
	public void testMissingArtifacts() throws MojoExecutionException {
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.plugin.addDependency(createDependecy("g3","a3"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(helper.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a3), Arrays.asList(g1a2)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a3, g3a4), Arrays.asList(g3a2, g3a5)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		assertEquals(String.format(
		    "[WARNING] Could not resolve artifact: [g1:a2:jar:1.0.0:compile]%n" +
        	"[WARNING] Could not resolve artifact: [g3:a2:jar:1.0.0:compile]%n" +
			"[WARNING] Could not resolve artifact: [g3:a5:jar:1.0.0:compile]%n"), helper.logStream.toString());

		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a5));
	}
	
	@Test
	public void testArtifactResolutionExceptions() throws MojoExecutionException {
		
		helper.mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_DEBUG, "test", new PrintStream(helper.logStream))));
		
        Artifact g1a1 = createArtifact("g1", "a1");
        Artifact g1a2 = createArtifact("g1", "a2");
        Artifact g1a3 = createArtifact("g1", "a3");
        Artifact g2a1 = createArtifact("g2", "a1");
        Artifact g3a1 = createArtifact("g3", "a1");
        Artifact g3a2 = createArtifact("g3", "a2");
        Artifact g3a3 = createArtifact("g3", "a3");
        Artifact g3a4 = createArtifact("g3", "a4");
        Artifact g3a5 = createArtifact("g3", "a5");
		List<Artifact> artifacts = Arrays.asList(helper.fitnesseArtifact, g1a1, g2a1, g3a3);
			
		helper.plugin.addDependency(createDependecy("g1","a1"));
		helper.plugin.addDependency(createDependecy("g2","a1"));
		helper.plugin.addDependency(createDependecy("g3","a3"));
		helper.mojo.pluginDescriptor.setArtifacts(artifacts);
		
		when(helper.artifactResolver.resolve(any(ArtifactResolutionRequest.class)))
		    .thenReturn(createArtifactResolutionResult(Collections.singleton(helper.fitnesseArtifact)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g1a1, g1a3), new ArtifactResolutionException("TEST", g1a2)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g2a1)))
		    .thenReturn(createArtifactResolutionResult(Arrays.asList(g3a1, g3a3, g3a4),
	    		new MultipleArtifactsNotFoundException(g3a3, Arrays.asList(g3a1, g3a4), Arrays.asList(g3a2, g3a5), null)));
		
		assertEquals("\n", helper.mojo.calcWikiFormatClasspath());
		//System.out.print(logStream.toString());
		assertTrue(helper.logStream.toString().contains(String.format("org.apache.maven.artifact.resolver.ArtifactResolutionException: TEST%n  g1:a2:jar:1.0.0")));
		assertTrue(helper.logStream.toString().contains(String.format("org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException: Missing:\n----------\n1) g3:a2:jar:1.0.0%n%n")));
		assertTrue(helper.logStream.toString().contains(String.format("1) g3:a2:jar:1.0.0%n%n  Try downloading the file manually from the project website.")));
		assertTrue(helper.logStream.toString().contains(String.format("2) g3:a5:jar:1.0.0%n%n  Try downloading the file manually from the project website.")));
		assertTrue(helper.logStream.toString().contains(String.format("\n----------\n2 required artifacts are missing.\n\nfor artifact: %n  g3:a3:jar:1.0.0")));
		
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(helper.fitnesseArtifact));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g1a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g2a1));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a1));
		verify(helper.mojo.fitNesseHelper, never())
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a2));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a3));
		verify(helper.mojo.fitNesseHelper, times(1))
		    .formatAndAppendClasspathArtifact(any(StringBuilder.class), eq(g3a4));
		verify(helper.mojo.fitNesseHelper, never())
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
            groupId, artifactId, "1.0.0", "compile", "jar", null, helper.artifactHandler);
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
