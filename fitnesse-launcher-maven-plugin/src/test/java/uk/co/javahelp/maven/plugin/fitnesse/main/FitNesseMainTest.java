package uk.co.javahelp.maven.plugin.fitnesse.main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.PrintStreamLogger;
import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;
import fitnesse.Arguments;
import fitnesse.FitNesse;

public class FitNesseMainTest {
	
	private static final String PORT = "9124"; // Using default port disturbs other tests for some reason

	@After
	public void tearDown() {
		ByteArrayOutputStream logStream = new ByteArrayOutputStream();
		Log log = new DefaultLog(new PrintStreamLogger(
			Logger.LEVEL_INFO, "test", new PrintStream(logStream)));
       	new FitNesseHelper(log).shutdownFitNesseServer(PORT);
	}
	
	@Ignore
	@Test
	public void testLaunchFailure() throws Exception {
		Arguments arguments = new Arguments();
		//arguments.setCommand("command");
		arguments.setPort(PORT);
		FitNesse fitnesse = FitNesseMain.launchFitNesse(arguments);
		Assert.assertNotNull(fitnesse);
		try {
			FitNesseMain.launchFitNesse(arguments);
			Assert.fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			Assert.assertEquals("FitNesse could not be launched", e.getMessage());
		}
	}
}
