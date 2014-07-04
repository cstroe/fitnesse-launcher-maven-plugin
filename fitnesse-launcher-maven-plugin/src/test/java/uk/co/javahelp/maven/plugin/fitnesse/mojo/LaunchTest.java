package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LaunchTest {

	@Test
	public void testCalcPageNameAndTypeSuite() {
		
		Launch launch = new Launch("SuiteName", null);
		assertEquals("SuiteName", launch.getPageName());
		assertEquals(Launch.PAGE_TYPE_SUITE, launch.getPageType());
	}
		
	@Test
	public void testCalcPageNameAndTypeTest() {
		
		Launch launch = new Launch(null, "SuiteName.NestedSuite.TestName");
		assertEquals("SuiteName.NestedSuite.TestName", launch.getPageName());
		assertEquals(Launch.PAGE_TYPE_TEST, launch.getPageType());
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalBoth() {
		try {
			Launch launch = new Launch("SuiteName", "SuiteName.NestedSuite.TestName");
			launch.getPageName();
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
			Launch launch = new Launch(suite, test);
			launch.getPageName();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("No suite or test page specified", e.getMessage());
		}
	}
}
