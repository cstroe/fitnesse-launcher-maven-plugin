package uk.co.javahelp.maven.plugin.fitnesse.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.PrintStreamLogger;
import fitnesse.junit.TestHelper;

public class FitNesseHelperTest {

	private FitNesseHelper fitNesseHelper;
	
    private ByteArrayOutputStream logStream;
    
	@Before
	public void setUp() {
		
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
}
