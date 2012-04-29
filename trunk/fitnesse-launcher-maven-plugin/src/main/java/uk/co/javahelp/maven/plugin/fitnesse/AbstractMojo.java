package uk.co.javahelp.maven.plugin.fitnesse;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import fitnesse.Arguments;
import fitnesse.junit.TestHelper;
import fitnesseMain.FitNesseMain;

public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    private static final String UTF8 = "UTF-8";

    /**
     * Used to look up Artifacts in the remote repository.
     * 
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    protected ArtifactResolver resolver;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List<ArtifactRepository> remoteArtifactRepositories;

    /**
     * @parameter expression="${fitnesse.port}" default-value="9123"
     */
    protected Integer port;

    /**
     * @parameter expression="${fitnesse.test.resource.directory}" default-value="src/test/fitnesse"
     */
    protected String testResourceDirectory;

    /**
     * @parameter expression="${fitnesse.working}" default-value="${project.build.directory}/fitnesse"
     */
    protected String workingDir;

    /**
     * @parameter expression="${fitnesse.root}" default-value="FitNesseRoot"
     */
    protected String root;

    /**
     * @parameter expression="${fitnesse.logDir}"
     */
    protected String logDir;

    /**
     * @see http://fitnesse.org/FitNesse.UserGuide.SymbolicLinks
     * @parameter expression="${fitnesse.createSymLink}"
     */
    protected boolean createSymLink;

    /**
     * The summary file to write integration test results to.
     * 
     * @parameter expression="${fitnesse.working}/results/failsafe-summary.xml"
     * @required
     */
    protected File summaryFile;

    /**
     * This is where build results go.
     * 
     * @parameter default-value="${fitnesse.working}/results"
     * @required
     */
    protected File resultsDir;

    /**
     * This is where build results go.
     * 
     * @parameter default-value="${fitnesse.working}/reports"
     * @required
     */
    protected File reportsDir;

    /**
     * @parameter expression="${fitnesse.suite}"
     */
    protected String suite;

    /**
     * @parameter expression="${fitnesse.test}"
     */
    protected String test;

    /**
     * @parameter expression="${fitnesse.suiteFilter}"
     */
    protected String suiteFilter;

    /**
     * @parameter expression="${fitnesse.excludeSuiteFilter}"
     */
    protected String excludeSuiteFilter;

    protected abstract void executeInternal() throws MojoExecutionException, MojoFailureException;

    public void execute() throws MojoExecutionException, MojoFailureException {
        exportProperties();
        executeInternal();
    }

    protected void runFitNesseServer() throws Exception {
        final Arguments arguments = new Arguments();
        arguments.setCommand(null);
        arguments.setInstallOnly(false);
        arguments.setOmitUpdates(true);
        arguments.setDaysTillVersionsExpire("0");
        arguments.setPort(this.port.toString());
        arguments.setRootPath(this.workingDir);
        arguments.setRootDirectory(this.root);
        if(this.logDir != null && !this.logDir.trim().equals(""))
            arguments.setLogDirectory(this.logDir);
        FitNesseMain.launchFitNesse(arguments);
		if(this.createSymLink) {
		    createSymLink(); 
		}
    }

    protected String[] calcPageNameAndType() throws MojoExecutionException {
        final boolean haveSuite = !isBlank(this.suite);
        final boolean haveTest = !isBlank(this.test);
        if (!haveSuite && !haveTest) {
            throw new MojoExecutionException("No suite or test page specified");
        } else if (haveSuite && haveTest) {
            throw new MojoExecutionException("Suite and test page parameters are mutually exclusive");
        }

        final String pageName = (haveSuite) ? this.suite : this.test;
        final String pageType = (haveSuite) ? TestHelper.PAGE_TYPE_SUITE : TestHelper.PAGE_TYPE_TEST;

        return new String[] { pageName, pageType };
    }

    protected void exportProperties() {
        final MavenProject project = (MavenProject) getPluginContext().get("project");
        final Properties projectProperties = project.getProperties();
        getLog().info("------------------------------------------------------------------------");
        final String mavenClasspath = calcWikiFormatClasspath(project);
        setSystemProperty("maven.classpath", mavenClasspath);

        // If a System property already exists, it has priority;
        // That way we can override with a -D on the command line
        for(String key : projectProperties.stringPropertyNames()) {
            final String value = System.getProperty(key, projectProperties.getProperty(key));
            setSystemProperty(key, value);
        }
        final String env = System.getProperty("env", System.getenv("username"));
        setSystemProperty("env", env);
        setSystemProperty("artifact", project.getArtifactId());
        setSystemProperty("version", project.getVersion());
        try {
            final String basedir = project.getBasedir().getCanonicalPath();
            setSystemProperty("basedir", basedir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLog().info("------------------------------------------------------------------------");
    }

    protected void setSystemProperty(final String key, final String value) {
        if(key != null && value != null &&
                !key.trim().equals("") &&
                !value.trim().equals("")) {
            getLog().info(String.format("Setting FitNesse variable [%s] to [%s]", key, value));
            System.setProperty(key, value);
        }
    }

    protected String calcWikiFormatClasspath(final MavenProject project) {

        final Set<Artifact> artifacts = new HashSet<Artifact>();
        final Set<Artifact> plugins = project.getPluginArtifacts();
        for ( Artifact plugin : plugins ) {
            if("uk.co.javahelp".equals(plugin.getGroupId()) && "fitnesse-launcher-maven-plugin".equals(plugin.getArtifactId())) {
                artifacts.addAll(resolveArtifactTransitively(plugin));
            }
        }
        final StringBuilder wikiFormatClasspath = new StringBuilder();
        for (Artifact artifact : artifacts) {
            artifact.getDependencyTrail();
            if(artifact.getFile() != null) {
                getLog().debug(String.format("Adding artifact to FitNesse classpath [%s]", artifact));
            	wikiFormatClasspath.append("!path ");
            	wikiFormatClasspath.append(artifact.getFile().getPath());
            	wikiFormatClasspath.append("\n");
            } else {
                getLog().debug(String.format("File for artifact [%s] is not found", artifact));
            }
        }
        return wikiFormatClasspath.toString();
    }

    protected Set<Artifact> resolveArtifactTransitively(final Artifact artifact) {
        final ArtifactResolutionRequest request = new ArtifactResolutionRequest()
            .setArtifact( artifact )
			.setResolveRoot( true )
			.setResolveTransitively( true )
			.setRemoteRepositories( this.remoteArtifactRepositories )
			.setLocalRepository( this.localRepository );
		final ArtifactResolutionResult result = this.resolver.resolve(request);
        if(result.isSuccess()) {
    		final Set<Artifact> dependencies = result.getArtifacts();
			return dependencies;
        }
        for(Artifact missing : result.getMissingArtifacts()) {
    		getLog().info(String.format("Could not resolve artifact: [%s]", missing));
        }
        if(result.hasExceptions() && getLog().isDebugEnabled()) {
            for(Exception exception : result.getExceptions()) {
    		    getLog().debug(exception);
            }
        }
        return Collections.emptySet();
    }

    /**
     * Note: Through experiment I've found that we can safely send duplicate 'create SymLink' requests - FitNesse isn't bothered
     * @see http://fitnesse.org/FitNesse.UserGuide.SymbolicLinks
     */
    protected void createSymLink() throws MojoExecutionException, MojoFailureException {
        final String linkName = calcLinkName();
        final String linkPath = calcLinkPath(linkName);

        HttpURLConnection connection = null;
        try {
            final String urlPath = 
                String.format("/root?responder=symlink&linkName=%s&linkPath=%s&submit=%s",
                URLEncoder.encode(linkName, UTF8), URLEncoder.encode(linkPath, UTF8), URLEncoder.encode("Create/Replace", UTF8));
            final URL url = new URL("http", "localhost", this.port, urlPath);
            getLog().info("Calling " + url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            final int responseCode = connection.getResponseCode();
            getLog().info("Response code: " + responseCode);
        } catch (Exception e) {
            throw new MojoExecutionException("Exception shutting down FitNesse", e);
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private String calcLinkName() throws MojoExecutionException {
        final String[] pageNameAndType = calcPageNameAndType();
        return pageNameAndType[0];
    }

    private String calcLinkPath(final String linkName) throws MojoExecutionException {
            final MavenProject project = (MavenProject) getPluginContext().get("project");
            final StringBuilder linkPath = new StringBuilder(
                project.getBasedir().toURI().toString()
                    .replaceFirst("/[A-Z]:", "")
                    .replaceFirst(":", "://"));
            linkPath.append(this.testResourceDirectory);
            linkPath.append(File.separatorChar);
            linkPath.append(linkName);
            return linkPath.toString();
    }

    private boolean isBlank(final String string) {
        return string == null || string.trim().equals("");
    }
}
