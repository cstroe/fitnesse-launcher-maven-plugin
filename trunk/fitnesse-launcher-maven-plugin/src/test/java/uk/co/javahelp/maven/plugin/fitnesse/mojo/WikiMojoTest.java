package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;
import uk.co.javahelp.maven.plugin.fitnesse.util.Interrupter;

public class WikiMojoTest {

	private WikiMojo mojo;
	
    private FitNesseHelper fitNesseHelper;
	
	@Before
	public void setUp() {
		fitNesseHelper = mock(FitNesseHelper.class);
		
		mojo = new WikiMojo();
		mojo.fitNesseHelper = this.fitNesseHelper;
		mojo.port = 8787;
		mojo.workingDir = "fitnesse";
		mojo.root = "FitNesseRoot";
		mojo.project = new MavenProject();
		mojo.project.setFile(new File(getClass().getResource("pom.xml").getFile()));
	}
	
	@Test
	public void testWikiMojoBasic() throws Exception {
		
		mojo.createSymLink = false;
		
		new Interrupter(Thread.currentThread(), 100L).start();
		mojo.executeInternal();
		
		verify(fitNesseHelper, times(1)).runFitNesseServer("8787", mojo.workingDir, mojo.root, mojo.logDir);
		verify(fitNesseHelper, never()).createSymLink(anyString(), anyString(), any(File.class), anyString(), anyInt());
	}
	
	@Test
	public void testWikiMojoCreateSymLink() throws Exception {
		
		mojo.createSymLink = true;
		mojo.suite = "suite";
		mojo.test = "test";
		mojo.testResourceDirectory = "testResourceDirectory";
		
		new Interrupter(Thread.currentThread(), 100L).start();
		mojo.executeInternal();
		
		verify(fitNesseHelper, times(1)).runFitNesseServer("8787", mojo.workingDir, mojo.root, mojo.logDir);
		verify(fitNesseHelper, times(1)).createSymLink(mojo.suite, mojo.test, mojo.project.getBasedir(), mojo.testResourceDirectory, 8787);
	}
}
