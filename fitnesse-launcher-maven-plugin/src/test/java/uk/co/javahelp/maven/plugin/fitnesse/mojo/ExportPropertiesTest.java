package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import org.codehaus.plexus.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public class ExportPropertiesTest {

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
        
		ArtifactResolutionResult result = new ArtifactResolutionResult();
		result.setArtifacts(Collections.singleton(this.fitnesseArtifact));
		
		when(artifactResolver.resolve(any(ArtifactResolutionRequest.class))).thenReturn(result);
		
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
		when(mojo.fitNesseHelper.formatAndAppendClasspathArtifact(
			any(StringBuilder.class), eq(fitnesseArtifact)))
				.then(new Answer<StringBuilder>() {
					@Override
					public StringBuilder answer(InvocationOnMock invocation) {
						StringBuilder sb = (StringBuilder) invocation.getArguments()[0];
						sb.append("TEST.CLASSPATH\n");
						return sb;
					}
				});
		
		mojo.pluginDescriptor = new PluginDescriptor();
		mojo.pluginDescriptor.setGroupId(pluginArtifact.getGroupId());
		mojo.pluginDescriptor.setArtifactId(pluginArtifact.getArtifactId());
		mojo.pluginDescriptor.setArtifacts(Collections.singletonList(this.fitnesseArtifact));
		mojo.project.setPluginArtifacts(Collections.singleton(this.pluginArtifact));
		mojo.project.setBuild(build);
		mojo.project.setFile(new File(getClass().getResource("pom.xml").getPath()));
		mojo.project.setArtifactId("ARTIFACT_ID");
		mojo.project.setVersion("VERSION");
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
	}
	
	@Test
	public void testExportPropertiesBasic() {
		
		mojo.exportProperties();
		
	    commonAssertions();
	}
	
	@Test
	public void testExportPropertiesExtraProperties() {
		mojo.project.getModel().addProperty("username", "batman");
		mojo.project.getModel().addProperty("password", "Holy Mashed Potato!");
		
		mojo.exportProperties();
		
	    commonAssertions();
	    
		assertEquals("batman", System.getProperty("username"));
		assertEquals("Holy Mashed Potato!", System.getProperty("password"));
		
		assertTrue(logStream.toString().contains("[INFO] Setting FitNesse variable [username] to [batman]"));
		assertTrue(logStream.toString().contains("[INFO] Setting FitNesse variable [password] to [Holy Mashed Potato!]"));
	}
	
	@Test
	public void testExportPropertiesPropertiesOverride() {
		mojo.project.getModel().addProperty("username", "batman");
		mojo.project.getModel().addProperty("password", "Holy Mashed Potato!");
		
		System.setProperty("username", "robin");
		System.setProperty("version", "NOT OVERRIDDEN");
		
		mojo.exportProperties();
		
	    commonAssertions();
	    
		assertEquals("robin", System.getProperty("username"));
		assertEquals("Holy Mashed Potato!", System.getProperty("password"));
		
		assertTrue(logStream.toString().contains("[INFO] Setting FitNesse variable [username] to [robin]"));
		assertTrue(logStream.toString().contains("[INFO] Setting FitNesse variable [password] to [Holy Mashed Potato!]"));
	}
	
	private void commonAssertions() {
		String expectedBasedir = mojo.project.getFile().getParent();
		assertEquals("\nTEST.CLASSPATH\n", System.getProperty("maven.classpath"));
		assertEquals("ARTIFACT_ID", System.getProperty("artifact"));
		assertEquals("VERSION", System.getProperty("version"));
		assertEquals(expectedBasedir, System.getProperty("basedir"));
		assertTrue(logStream.toString().startsWith("[INFO] ------------------------------------------------------------------------"));
		assertTrue(logStream.toString().endsWith("[INFO] ------------------------------------------------------------------------\n"));
		assertTrue(logStream.toString().contains("[INFO] Setting FitNesse variable [maven.classpath] to [\nTEST.CLASSPATH\n]"));
		assertTrue(logStream.toString().contains("[INFO] Setting FitNesse variable [artifact] to [ARTIFACT_ID]"));
		assertTrue(logStream.toString().contains("[INFO] Setting FitNesse variable [version] to [VERSION]"));
		assertTrue(logStream.toString().contains(String.format("[INFO] Setting FitNesse variable [basedir] to [%s]", expectedBasedir)));
	}
}
