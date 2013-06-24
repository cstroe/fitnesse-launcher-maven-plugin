package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.PrintStreamLogger;
import fitnesse.Arguments;
import fitnesse.junit.TestHelper;

public class FitNesseHelperTest {

	private FitNesseHelper fitNesseHelper;
	
	private ArtifactHandler artifactHandler;
	
    private ByteArrayOutputStream logStream;
    
	@Before
	public void setUp() {
		artifactHandler = mock(ArtifactHandler.class);
		
		logStream = new ByteArrayOutputStream();
		Log log = new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream)));
		
		fitNesseHelper = new FitNesseHelper(log);
	}
	
	@Test
	public void testCalcPageNameAndTypeSuite() {
		
		String[] result = fitNesseHelper.calcPageNameAndType("SuiteName", null);
		assertEquals(2, result.length);
		assertEquals("SuiteName", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_SUITE, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeTest() {
		
		String[] result = fitNesseHelper.calcPageNameAndType(null, "SuiteName.NestedSuite.TestName");
		assertEquals(2, result.length);
		assertEquals("SuiteName.NestedSuite.TestName", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_TEST, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalBoth() {
		try {
			fitNesseHelper.calcPageNameAndType("SuiteName", "SuiteName.NestedSuite.TestName");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Suite and test page parameters are mutually exclusive", e.getMessage());
		}
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalNeither() {
	    assertCalcPageNameAndTypeIllegalNeither(null, null);
	    assertCalcPageNameAndTypeIllegalNeither(" ", " ");
	}
	
	private void assertCalcPageNameAndTypeIllegalNeither(String suite, String test) {
		try {
			fitNesseHelper.calcPageNameAndType(suite, test);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("No suite or test page specified", e.getMessage());
		}
	}
		
	@Test
	public void testFormatAndAppendClasspathArtifact() {
        String jarPath = new File(getClass().getResource("/dummy.jar").getPath()).getPath();
        Artifact artifact = new DefaultArtifact(
            "org.fitnesse", "fitnesse", "20121220", "compile", "jar", null, artifactHandler);
        artifact.setFile(new File(jarPath));
        
		StringBuilder sb = new StringBuilder();
		assertSame(sb, fitNesseHelper.formatAndAppendClasspathArtifact(sb, artifact));
		
		assertEquals("!path " + jarPath + "\n", sb.toString());
	}
		
	@Test
	public void testLaunchFitNesseServer() throws Exception {
		File logDir = new File(System.getProperty("java.io.tmpdir"), "fitnesse-launcher-logs");
		// Clean out logDir, as it might still exist from a previous run, 
		// because Windows doesn't always delete this file on exit
  		FileUtils.deleteQuietly(logDir);
		assertLaunchFitNesseServer(null);
		assertLaunchFitNesseServer(" ");
		assertLaunchFitNesseServer(logDir.getCanonicalPath());
		String[] logFiles = logDir.list();
		assertEquals(1, logFiles.length);
		assertTrue(logFiles[0].matches("fitnesse[0-9]+\\.log"));
		FileUtils.forceDeleteOnExit(logDir);
	}
		
	public void assertLaunchFitNesseServer(String logDir) throws Exception {
		String port = String.valueOf(Arguments.DEFAULT_COMMAND_PORT);
		File working = new File(System.getProperty("java.io.tmpdir"), "fitnesse-launcher-test");
		fitNesseHelper.launchFitNesseServer(port, working.getCanonicalPath(), "FitNesseRoot", logDir);
		URL local = new URL("http://localhost:" + port);
		InputStream in = local.openConnection().getInputStream();
		try {
			String content = IOUtils.toString(in);
			assertTrue(content.startsWith("<!DOCTYPE html>"));
			assertTrue(content.contains("<title>Page doesn't exist. Edit: FrontPage</title>"));
		} finally {
			IOUtils.closeQuietly(in);
    		fitNesseHelper.shutdownFitNesseServer(port);
    		Thread.sleep(100L);
			FileUtils.deleteQuietly(working);
		}
	}
		
	@Test
	public void testShutdownFitNesseServerOk() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new OkHandler("/", "responder=shutdown"));
	    server.start();
	    
	    try {
			fitNesseHelper.shutdownFitNesseServer(String.valueOf(port));
		} finally {
    		server.stop();
		}
	}
	
	@Test
	public void testShutdownFitNesseServerNotRunning() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		fitNesseHelper.shutdownFitNesseServer(String.valueOf(port));
		assertEquals(String.format("[INFO] FitNesse already not running.%n"), logStream.toString());
	}
	
	@Test
	public void testShutdownFitNesseServerDisconnect() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new DisconnectingHandler(server));
	    server.start();
	    
	    try {
			fitNesseHelper.shutdownFitNesseServer(String.valueOf(port));
			
			assertTrue(logStream.toString().startsWith(String.format("[ERROR] %njava.io.IOException: Could not parse Response")));
		} finally {
    		server.stop();
		}
	}
		
	@Test
	public void testCreateSymLinkOkSuite() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new OkHandler("/root", 
			"responder=symlink&linkName=SuiteName&linkPath=file%3A%2F%2F%2Ftmp%2FBASEDIR%2FTEST_RESOURCE_DIR%2FSuiteName&submit=Create%2FReplace"));
	    server.start();
	    
	    try {
			int response = fitNesseHelper.createSymLink(
				"SuiteName.NestedSuite", null, new File("/tmp", "BASEDIR"),
				"/TEST_RESOURCE_DIR", port);
			
			assertEquals(200, response);
			assertEquals(
			    "[INFO] Calling http://localhost:9123/root?responder=symlink&linkName=SuiteName&linkPath=file%3A%2F%2F%2Ftmp%2FBASEDIR%2FTEST_RESOURCE_DIR%2FSuiteName&submit=Create%2FReplace" +
				String.format("%n[INFO] Response code: 200%n"), logStream.toString());

		} finally {
    		server.stop();
		}
	}
		
	@Test
	public void testCreateSymLinkOkTest() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new OkHandler("/root", 
			"responder=symlink&linkName=SuiteName&linkPath=file%3A%2F%2F%2Ftmp%2FBASEDIR%2FTEST_RESOURCE_DIR%2FSuiteName&submit=Create%2FReplace"));
	    server.start();
	    
	    try {
			int response = fitNesseHelper.createSymLink(
				null, "SuiteName.NestedSuite.TestName", new File("/tmp", "BASEDIR"),
				"/TEST_RESOURCE_DIR", port);
			
			assertEquals(200, response);
			assertEquals(
			    "[INFO] Calling http://localhost:9123/root?responder=symlink&linkName=SuiteName&linkPath=file%3A%2F%2F%2Ftmp%2FBASEDIR%2FTEST_RESOURCE_DIR%2FSuiteName&submit=Create%2FReplace" +
				String.format("%n[INFO] Response code: 200%n"), logStream.toString());

		} finally {
    		server.stop();
		}
	}
		
	@Test
	public void testCreateSymLinkDisconnect() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new DisconnectingHandler(server));
	    server.start();
	    
	    try {
			fitNesseHelper.createSymLink(
				"SuiteName.NestedSuite", null, new File("/tmp", "BASEDIR"),
				"/TEST_RESOURCE_DIR", port);
			
			fail("Expected ConnectException");

		} catch(ConnectException e) {
			// OK
		} finally {
    		server.stop();
		}
	}
	
	private static class OkHandler extends AbstractHandler {
		
		private String expectedRequestUri;
		
		private String expectedQueryString;

		public OkHandler(String expectedRequestUri, String expectedQueryString) {
			this.expectedRequestUri = expectedRequestUri;
			this.expectedQueryString = expectedQueryString;
		}

		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			assertEquals(expectedRequestUri, request.getRequestURI());
			assertEquals(expectedQueryString, request.getQueryString());
			
			response.addHeader("Server", "FitNesse");
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
	}
	
	private static class DisconnectingHandler extends AbstractHandler {
		
		private Server server;

		public DisconnectingHandler(Server server) {
			this.server = server;
		}

		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			try {
				server.stop();
			} catch (Exception e) {
				// Swallow
			}
		}
	}
}
