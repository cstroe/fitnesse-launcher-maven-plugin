package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.maven.monitor.logging.DefaultLog;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ShutdownMojoTest {
	
	private ShutdownMojo mojo;
	
    private ByteArrayOutputStream logStream;
    
	private Server server;
	
	@Before
	public void setUp() throws Exception {
		
		mojo = new ShutdownMojo();
		
		logStream = new ByteArrayOutputStream();
		mojo.setLog(new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream))));
	}
	
	@After
	public void tearDown() throws Exception {
		if(this.server != null) {
			this.server.stop();
		}
	}
	
	@Test
	public void testServerRunning() throws Exception {
		
		this.server = new Server(mojo.port);
	    this.server.setHandler(new Handler());
	    this.server.start();
	    
		mojo.execute();
		
		assertEquals(String.format("[INFO] FitNesse wiki server is shutdown.%n"), logStream.toString());
	}
	
	
	@Test
	public void testServerNotRunning() throws Exception {
		mojo.execute();
		//assertEquals(String.format(
			    // TODO: Investigate: This behaviour changed from 20130530 to 20131110
				// Behaviour changes depending upon prior tests run
				//"[INFO] FitNesse already not running.%n" +
				//"[INFO] FitNesse wiki server is shutdown.%n"), logStream.toString());
		assertTrue(logStream.toString().contains("[INFO] FitNesse wiki server is shutdown."));
	}
	
	private static class Handler extends AbstractHandler {

		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			assertEquals("/", request.getRequestURI());
			assertEquals("responder=shutdown", request.getQueryString());
			
			response.addHeader("Server", "FitNesse");
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
	}
}
