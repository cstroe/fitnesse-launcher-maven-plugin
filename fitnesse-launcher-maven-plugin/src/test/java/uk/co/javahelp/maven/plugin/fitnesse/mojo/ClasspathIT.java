package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * http://maven.apache.org/shared/maven-verifier/apidocs/index.html
 */
public class ClasspathIT 
{
	private static String groupId = getProperty("it.groupId");
	
	private static String artifactId = getProperty("it.artifactId");
	
	private static String version = getProperty("it.version");
	
    private static File testDir = new File(getProperty("maven.test.tmpdir"), artifactId + "-" + version);	
    
	@Before
	public void setUp() {
		printf("Testing complete project %s:%s:%s ...", groupId, artifactId, version);
	}
	
	@After
	public void tearDown() throws IOException {
		printf("Output available in %s", new File(testDir, "log.txt").getCanonicalPath());
	}
	
	@Test
	public void testRunTests() throws Exception {
        Verifier verifier = new Verifier(testDir.getAbsolutePath(), null, true, false );
        verifier.deleteArtifacts(groupId, artifactId, version);
        
		printf("mvn clean install -P auto");
        verifier.setCliOptions(asList("-P auto"));
        verifier.executeGoals(asList("clean", "install"));
        
        verifier.verifyErrorFreeLog();
        verifier.verifyTextInLog("38 right, 0 wrong, 0 ignored, 0 exceptions");
        verifier.assertArtifactPresent(groupId, artifactId, version, "jar");
    }
		
	private void printf(String string, Object... args) {
		System.out.println(String.format(string, args));
	}
}
