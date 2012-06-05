package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		
		String[] result = fitNesseHelper.calcPageNameAndType("SUITE_NAME", null);
		assertEquals(2, result.length);
		assertEquals("SUITE_NAME", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_SUITE, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeTest() {
		
		String[] result = fitNesseHelper.calcPageNameAndType(null, "TEST_NAME");
		assertEquals(2, result.length);
		assertEquals("TEST_NAME", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_TEST, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalBoth() {
		try {
			fitNesseHelper.calcPageNameAndType("SUITE_NAME", "TEST_NAME");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Suite and test page parameters are mutually exclusive", e.getMessage());
		}
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalNeither() {
		try {
			fitNesseHelper.calcPageNameAndType(null, null);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("No suite or test page specified", e.getMessage());
		}
	}
		
	@Test
	public void testFormatAndAppendClasspathArtifact() {
        String jarPath = new File(getClass().getResource("/dummy.jar").getPath()).getPath();
        Artifact artifact = new DefaultArtifact(
            "org.fitnesse", "fitnesse", "20111025", "compile", "jar", null, artifactHandler);
        artifact.setFile(new File(jarPath));
        
		StringBuilder sb = new StringBuilder();
		assertSame(sb, fitNesseHelper.formatAndAppendClasspathArtifact(sb, artifact));
		
		assertEquals("!path " + jarPath + "\n", sb.toString());
	}
		
	@Test
	public void testCreateSymLink() throws Exception {
		int port = Arguments.DEFAULT_COMMAND_PORT;
		Server server = new Server(port);
	    server.setHandler(new Handler());
	    server.start();
	    
	    try {
			int response = fitNesseHelper.createSymLink(
				"SUITE_NAME", null, new File("/tmp", "BASEDIR"),
				"/TEST_RESOURCE_DIR", port);
			
			assertEquals(200, response);
			assertEquals(
			    "[INFO] Calling http://localhost:9123/root?responder=symlink&linkName=SUITE_NAME&linkPath=file%3A%2F%2F%2Ftmp%2FBASEDIR%2FTEST_RESOURCE_DIR%2FSUITE_NAME&submit=Create%2FReplace" +
				String.format("%n[INFO] Response code: 200%n"), logStream.toString());

		} finally {
    		server.stop();
		}
	}
	
	private static class Handler extends AbstractHandler {

		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			assertEquals("/root", request.getRequestURI());
			assertEquals("responder=symlink&linkName=SUITE_NAME&linkPath=file%3A%2F%2F%2Ftmp%2FBASEDIR%2FTEST_RESOURCE_DIR%2FSUITE_NAME&submit=Create%2FReplace", request.getQueryString());
			
			response.addHeader("Server", "FitNesse");
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
	}
}
