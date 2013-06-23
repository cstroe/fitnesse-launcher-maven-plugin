package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class FitNesseTest {

	/**
	 * http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
	 */
	public static <C> void assertUtilityClass(Class<C> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		assertTrue("Utility class should be 'final'", Modifier.isFinal(clazz.getModifiers()));
		//assertTrue("Utility class should be 'public'", Modifier.isPublic(clazz.getModifiers()));
		Constructor<?>[] ctors = clazz.getDeclaredConstructors();
		assertEquals("Utility class should only have one constructor", 1, ctors.length);
		Constructor<?> ctor = ctors[0];
		assertFalse("Utility class constructor should be inaccessible", ctor.isAccessible());
		ctor.setAccessible(true); // obviously we'd never do this in production
		assertEquals("You'd expect the construct to return the expected type", clazz, ctor.newInstance().getClass());
	}
	
	@Test
	public void testFitNesse() throws InstantiationException, IllegalAccessException, InvocationTargetException {
	    assertUtilityClass(FitNesse.class);
	}
}
