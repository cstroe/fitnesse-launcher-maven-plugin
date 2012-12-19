package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

/**
 * http://maven.apache.org/shared/maven-verifier/apidocs/index.html
 */
public class ClasspathIT 
{
	@Test
	public void testRunTests() throws Exception {
        File testDir = ResourceExtractor.extractResourcePath(ClasspathIT.class, "/it",
        	new File(System.getProperty("maven.test.tmpdir", System.getProperty("java.io.tmpdir"))), true);

        Verifier verifier = new Verifier(testDir.getAbsolutePath(), null, true, false );
        verifier.deleteArtifacts( "uk.co.javahelp.fitnesse", "fitnesse-launcher-it", "1.1.0-SNAPSHOT" );
        
        verifier.executeGoal("install");
        
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("21 right, 0 wrong, 0 ignored, 0 exceptions");
        verifier.assertArtifactPresent( "uk.co.javahelp.fitnesse", "fitnesse-launcher-it", "1.1.0-SNAPSHOT", "jar" );
    }
}
