package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * http://maven.apache.org/shared/maven-verifier/apidocs/index.html
 */
@RunWith(Parameterized.class)
public class ExampleProjectsIT 
{
	private static String SEPARATOR = "-------------------------------------------------------";
	
	private static String groupId = getProperty("it.groupId");
	
	private static String[] artifactIds = getProperty("it.artifactIds").split(",");
	
	private static String[] artifactTestCounts = getProperty("it.artifact.testCounts").split(",");
	
	private static String[] artifactExts = getProperty("it.artifact.extensions").split(",");
	
	private static String version = getProperty("it.version");
	
    @Parameters
    public static Collection<Object[]> data() {
		assertEquals("Number of artifactIds must match number of test counts",
				artifactIds.length, artifactTestCounts.length);
		assertEquals("Number of artifactIds must match number of extensions",
				artifactIds.length, artifactExts.length);
		
		List<Object[]> params = new ArrayList<Object[]>();
		for(int i = 0 ; i < artifactIds.length ; i++) {
            params.add(new Object[]{artifactIds[i], artifactTestCounts[i], artifactExts[i]});
		}
        return params;
    }
	
	private String artifactId;
	
	private String testCount;
	
	private String ext;
	
    private File testDir;
	
	public ExampleProjectsIT(String artifactId, String testCount, String ext) {
		this.artifactId = artifactId;
		this.testCount = testCount;
		this.ext = ext;
        testDir = new File(getProperty("maven.test.tmpdir"), artifactId + "-" + version);	
	}

	@Before
	public void setUp() {
   		printf("Testing complete project %s:%s:%s ...", groupId, artifactId, version);
	}

	@After
	public void tearDown() throws IOException {
   		printf("Output available in %s", new File(testDir, "log.txt").getCanonicalPath());
   		printf(SEPARATOR);
	}

	@Test
	public void runProject() throws VerificationException, IOException {
		Verifier verifier = new Verifier(testDir.getAbsolutePath(), null, true, false );
		verifier.deleteArtifacts(groupId, artifactId, version);
			
		printf("mvn clean install -P auto");
		verifier.setCliOptions(asList("-P auto"));
		verifier.executeGoals(asList("clean", "install"));
			
		verifier.verifyErrorFreeLog();
		if(!"pom".equals(ext)) {
		    verifier.verifyTextInLog(format("%s right, 0 wrong, 0 ignored, 0 exceptions",testCount));
		}
		verifier.assertArtifactPresent(groupId, artifactId, version, ext);
    }
		
	private void printf(String string, Object... args) {
		System.out.println(format(string, args));
	}
}
