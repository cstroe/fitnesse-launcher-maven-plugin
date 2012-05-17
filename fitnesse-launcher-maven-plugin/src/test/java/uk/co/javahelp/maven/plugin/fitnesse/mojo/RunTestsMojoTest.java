package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;
import fitnesse.junit.TestHelper;
import fitnesse.slim.test.TestsInProgress;

public class RunTestsMojoTest {

	private RunTestsMojo mojo;
	
    private FitNesseHelper fitNesseHelper;
    
    private ByteArrayOutputStream logStream;
    
	@Before
	public void setUp() throws IOException {
		fitNesseHelper = mock(FitNesseHelper.class);
		
		File workingDir = new File(System.getProperty("java.io.tmpdir"), "unit_test_working");
		
		mojo = new RunTestsMojo();
		mojo.fitNesseHelper = this.fitNesseHelper;
		mojo.port = WikiMojoTest.PORT;
		mojo.workingDir = workingDir.getCanonicalPath();
		mojo.root = "FitNesseRoot";
		mojo.resultsDir = new File(System.getProperty("java.io.tmpdir"), "unit_test_results");
		mojo.reportsDir = new File(System.getProperty("java.io.tmpdir"), "unit_test_reports");
		mojo.summaryFile = new File(mojo.resultsDir, "failsafe-summary.xml");
		mojo.project = new MavenProject();
		mojo.project.setFile(new File(getClass().getResource("pom.xml").getPath()));
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
		
		FileUtils.deleteQuietly(workingDir);
		FileUtils.deleteQuietly(mojo.resultsDir);
		FileUtils.deleteQuietly(mojo.reportsDir);
		
		File root = new File(workingDir, mojo.root);
		root.mkdirs();
		FileUtils.copyDirectoryToDirectory(new File(getClass().getResource("/files").getPath()), root);
		FileUtils.copyDirectoryToDirectory(new File(getClass().getResource("/ExampleFitNesseTestSuite").getPath()), root);
	}
	
	/**
	 * We have to clean up the mess made by {@link TestsInProgress} and {@link PageInProgressFormatter}.
	 */
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File("FitNesseRoot"));
	}
	
	@Test
	public void testRunTestsMojoBasic() throws Exception {
		
		mojo.createSymLink = false;
		when(fitNesseHelper.calcPageNameAndType(anyString(), anyString()))
		    .thenReturn(new String[]{"ExampleFitNesseTestSuite", TestHelper.PAGE_TYPE_SUITE});
		
		mojo.executeInternal();
		
		verify(fitNesseHelper, never()).launchFitNesseServer(anyString(), anyString(), anyString(), anyString());
		verify(fitNesseHelper, never()).createSymLink(anyString(), anyString(), any(File.class), anyString(), anyInt());
		verify(fitNesseHelper, never()).shutdownFitNesseServer(anyString());
		
		assertEquals(
		    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<failsafe-summary result=\"255\" />\n", FileUtils.readFileToString(mojo.summaryFile));
		
		assertTrue(
			FileUtils.readFileToString(
				new File(mojo.resultsDir, "TEST-ExampleFitNesseTestSuite.xml")).matches(
			"<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"[0-9]+.[0-9]{4}\" failures=\"1\" name=\"ExampleFitNesseTestSuite\">" +
				"<properties></properties>" +
				"<testcase classname=\"ExampleFitNesseTestSuite\" time=\"[0-9]+.[0-9]{4}\" name=\"ExampleFitNesseTestSuite\">" +
					"<failure type=\"java.lang.AssertionError\" message=\" exceptions: 0 wrong: 1\">" +
					"</failure>" +
				"</testcase>" +
			"</testsuite>"));
		
		assertEquals(
		    IOUtils.toString(getClass().getResourceAsStream("ExampleFitNesseTestSuite.html")),
			FileUtils.readFileToString(new File(mojo.reportsDir, "ExampleFitNesseTestSuite.html")));
	}
	
	@Test
	public void testRunTestsMojoCreateSymLink() throws Exception {
		
		mojo.createSymLink = true;
		when(fitNesseHelper.calcPageNameAndType(anyString(), anyString()))
		    .thenReturn(new String[]{"ExampleFitNesseTestSuite", TestHelper.PAGE_TYPE_SUITE});
		
		mojo.executeInternal();
		
		verify(fitNesseHelper, times(1)).launchFitNesseServer(WikiMojoTest.PORT_STRING, mojo.workingDir, mojo.root, mojo.logDir);
		verify(fitNesseHelper, times(1)).createSymLink(mojo.suite, mojo.test, mojo.project.getBasedir(), mojo.testResourceDirectory, WikiMojoTest.PORT);
		verify(fitNesseHelper, times(1)).shutdownFitNesseServer(WikiMojoTest.PORT_STRING);
		
		assertEquals(
		    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<failsafe-summary result=\"255\" />\n", FileUtils.readFileToString(mojo.summaryFile));
		
		assertTrue(
			FileUtils.readFileToString(
				new File(mojo.resultsDir, "TEST-ExampleFitNesseTestSuite.xml")).matches(
			"<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"[0-9]+.[0-9]{4}\" failures=\"1\" name=\"ExampleFitNesseTestSuite\">" +
				"<properties></properties>" +
				"<testcase classname=\"ExampleFitNesseTestSuite\" time=\"[0-9]+.[0-9]{4}\" name=\"ExampleFitNesseTestSuite\">" +
					"<failure type=\"java.lang.AssertionError\" message=\" exceptions: 0 wrong: 1\">" +
					"</failure>" +
				"</testcase>" +
			"</testsuite>"));
		
		assertEquals(
		    IOUtils.toString(getClass().getResourceAsStream("ExampleFitNesseTestSuite.html")),
			FileUtils.readFileToString(new File(mojo.reportsDir, "ExampleFitNesseTestSuite.html")));
	}
}
