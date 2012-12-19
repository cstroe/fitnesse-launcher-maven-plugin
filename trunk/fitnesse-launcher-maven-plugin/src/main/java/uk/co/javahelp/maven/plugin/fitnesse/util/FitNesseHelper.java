package uk.co.javahelp.maven.plugin.fitnesse.util;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

import fitnesse.Arguments;
import fitnesse.Shutdown;
import fitnesse.junit.TestHelper;
import fitnesseMain.FitNesseMain;

public class FitNesseHelper {

    private static final String UTF8 = "UTF-8";
    
    private final Log log;

    public FitNesseHelper(final Log log) {
		this.log = log;
	}
    
    public StringBuilder formatAndAppendClasspathArtifact(final StringBuilder wikiFormatClasspath, final Artifact artifact) {
    	return formatAndAppendClasspath(wikiFormatClasspath, artifact.getFile().getPath());
	}
    
    public StringBuilder formatAndAppendClasspath(final StringBuilder wikiFormatClasspath, final String path) {
       	wikiFormatClasspath.append("!path ");
       	wikiFormatClasspath.append(path);
       	wikiFormatClasspath.append("\n");
       	return wikiFormatClasspath;
    }

	public void launchFitNesseServer(
    		final String port, final String workingDir, final String root, final String logDir) throws Exception {
        final Arguments arguments = new Arguments();
        arguments.setCommand(null);
        arguments.setInstallOnly(false);
        arguments.setOmitUpdates(true);
        arguments.setDaysTillVersionsExpire("0");
        arguments.setPort(port);
        arguments.setRootPath(workingDir);
        arguments.setRootDirectory(root);
        if(logDir != null && !logDir.trim().equals(""))
            arguments.setLogDirectory(logDir);
        FitNesseMain.launchFitNesse(arguments);
    }

	public void shutdownFitNesseServer(final String port) {
        try {
			Shutdown.main(new String[]{"-p", port});
		} catch (ConnectException e) {
			// If we get this specific exception,
			// we assume FitNesse is already not running
			if(!e.getMessage().startsWith("Connection refused")) {
               	this.log.error(e);
			}
		} catch (Exception e) {
           	this.log.error(e);
		}
    }

    /**
     * Note: Through experiment I've found that we can safely send duplicate 'create SymLink' requests - FitNesse isn't bothered
     * @throws IOException 
     * @see <a href="http://fitnesse.org/FitNesse.UserGuide.SymbolicLinks">FitNesse SymLink User Guide</a>
     */
    public int createSymLink(final String suite, final String test,
    		final File basedir, final String testResourceDirectory, final int port) throws IOException {
        final String linkName = calcLinkName(suite, test);
        final String linkPath = calcLinkPath(linkName, basedir, testResourceDirectory);

        HttpURLConnection connection = null;
        try {
            final String urlPath = 
                String.format("/root?responder=symlink&linkName=%s&linkPath=%s&submit=%s",
                URLEncoder.encode(linkName, UTF8), URLEncoder.encode(linkPath, UTF8), URLEncoder.encode("Create/Replace", UTF8));
            final URL url = new URL("http", "localhost", port, urlPath);
            this.log.info("Calling " + url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            final int responseCode = connection.getResponseCode();
            this.log.info("Response code: " + responseCode);
            return responseCode;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public String[] calcPageNameAndType(final String suite, final String test) {
        final boolean haveSuite = !isBlank(suite);
        final boolean haveTest = !isBlank(test);
        if (!haveSuite && !haveTest) {
            throw new IllegalArgumentException("No suite or test page specified");
        } else if (haveSuite && haveTest) {
            throw new IllegalArgumentException("Suite and test page parameters are mutually exclusive");
        }

        final String pageName = (haveSuite) ? suite : test;
        final String pageType = (haveSuite) ? TestHelper.PAGE_TYPE_SUITE : TestHelper.PAGE_TYPE_TEST;

        return new String[] { pageName, pageType };
    }

    private String calcLinkName(final String suite, final String test) {
        final String[] pageNameAndType = calcPageNameAndType(suite, test);
        return pageNameAndType[0];
    }

    private String calcLinkPath(final String linkName, final File basedir, final String testResourceDirectory) {
        final StringBuilder linkPath = new StringBuilder(
            basedir.toURI().toString()
                .replaceFirst("/[A-Z]:", "")
				.replaceFirst(":", "://"));
		linkPath.append(testResourceDirectory);
		linkPath.append("/");
		linkPath.append(linkName);
		return linkPath.toString();
    }

    public static boolean isBlank(final String string) {
        return string == null || string.trim().equals("");
    }
}
