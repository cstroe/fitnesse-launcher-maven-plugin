package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import uk.co.javahelp.maven.plugin.fitnesse.junit.TestHelper;

public class ExecutionTest {

	@Test
	public void testCalcPageNameAndTypeSuite() {
		
		String[] result = new Execution("SuiteName", null).calcPageNameAndType();
		assertEquals(2, result.length);
		assertEquals("SuiteName", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_SUITE, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeTest() {
		
		String[] result = new Execution(null, "SuiteName.NestedSuite.TestName").calcPageNameAndType();
		assertEquals(2, result.length);
		assertEquals("SuiteName.NestedSuite.TestName", result[0]);
		assertEquals(TestHelper.PAGE_TYPE_TEST, result[1]);
	}
		
	@Test
	public void testCalcPageNameAndTypeIllegalBoth() {
		try {
			new Execution("SuiteName", "SuiteName.NestedSuite.TestName").calcPageNameAndType();
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
			new Execution(suite, test).calcPageNameAndType();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("No suite or test page specified", e.getMessage());
		}
	}
}
