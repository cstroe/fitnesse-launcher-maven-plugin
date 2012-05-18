package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.io.File;
import java.io.IOException;
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
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    /**
     * Used to look up Artifacts in the remote repository.
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
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
     * Maven project, to be injected by Maven itself.
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;
    
    /**
     * @parameter expression="${plugin}"
     * @required
     */
    protected PluginDescriptor pluginDescriptor;

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
     * fitnesse-launcher-maven-plugin unpacks a fresh copy of FitNesse under /target;
     * Only your project specific FitNesse tests need go under src/test/fitnesse.
     * By setting 'createSymLink' to 'true', fitnesse-launcher-maven-plugin will
     * create a FitNesse SymLink directly to your test suite under src/test/fitnesse.
     * This is most useful when developing tests in 'wiki' mode,
     * as then you can directly scm commit your changes.
     * If you prefer to copy-resources from src/test/fitnesse into /target/fitnesse,
     * let 'createSymLink' be 'false'.
     * @see <a href="http://fitnesse.org/FitNesse.UserGuide.SymbolicLinks">FitNesse SymLink User Guide</a>
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
    
    protected FitNesseHelper fitNesseHelper;

    protected abstract void executeInternal() throws MojoExecutionException, MojoFailureException;

	@Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	this.fitNesseHelper = new FitNesseHelper(getLog());
        exportProperties();
        executeInternal();
    }

    private static final String LOG_LINE = "------------------------------------------------------------------------";
        
    protected void exportProperties() {
        final Properties projectProperties = this.project.getProperties();
        getLog().info(LOG_LINE);
        final String mavenClasspath = calcWikiFormatClasspath();
        setSystemProperty("maven.classpath", mavenClasspath);

        // If a System property already exists, it has priority;
        // That way we can override with a -D on the command line
        for(String key : projectProperties.stringPropertyNames()) {
            final String value = System.getProperty(key, projectProperties.getProperty(key));
            setSystemProperty(key, value);
        }
        setSystemProperty("artifact", this.project.getArtifactId());
        setSystemProperty("version", this.project.getVersion());
        try {
            final String basedir = this.project.getBasedir().getCanonicalPath();
            setSystemProperty("basedir", basedir);
        } catch (IOException e) {
        	getLog().error(e);
        }
        getLog().info(LOG_LINE);
    }

    protected void setSystemProperty(final String key, final String value) {
        if(key != null && value != null &&
                !key.trim().equals("") &&
                !value.trim().equals("")) {
            getLog().info(String.format("Setting FitNesse variable [%s] to [%s]", key, value));
            System.setProperty(key, value);
        }
    }

    protected String calcWikiFormatClasspath() {
        final Set<Artifact> artifacts = new HashSet<Artifact>();
        
        // We should always have FitNesse itself on the FitNesse classpath!
       	artifacts.addAll(resolveDependencyKey("org.fitnesse:fitnesse"));
                
        final List<Dependency> dependecies = 
            this.project.getPlugin(this.pluginDescriptor.getPluginLookupKey()).getDependencies();
        
        for(Dependency dependency : dependecies) {
        	final String key = dependency.getGroupId() + ":" + dependency.getArtifactId();
        	artifacts.addAll(resolveDependencyKey(key));
        }
        final StringBuilder wikiFormatClasspath = new StringBuilder("\n");
        for (Artifact artifact : artifacts) {
            if(artifact.getFile() != null) {
                getLog().debug(String.format("Adding artifact to FitNesse classpath [%s]", artifact));
                this.fitNesseHelper.formatAndAppendClasspathArtifact(wikiFormatClasspath, artifact);
            } else {
                getLog().warn(String.format("File for artifact [%s] is not found", artifact));
            }
        }
        return wikiFormatClasspath.toString();
    }

    private Set<Artifact> resolveDependencyKey(final String key) {
       	final Artifact artifact = this.pluginDescriptor.getArtifactMap().get(key);
       	if(artifact == null) {
            getLog().warn(String.format("Lookup for artifact [%s] failed", key));
            return Collections.emptySet();
       	}
        return resolveArtifactTransitively(artifact);
    }

    private Set<Artifact> resolveArtifactTransitively(final Artifact artifact) {
        final ArtifactResolutionRequest request = new ArtifactResolutionRequest()
            .setArtifact( artifact )
			.setResolveRoot( true )
			.setResolveTransitively( true )
			.setRemoteRepositories( this.remoteArtifactRepositories )
			.setLocalRepository( this.localRepository );
		final ArtifactResolutionResult result = this.resolver.resolve(request);
        if(!result.isSuccess()) {
            for(Artifact missing : result.getMissingArtifacts()) {
    			getLog().warn(String.format("Could not resolve artifact: [%s]", missing));
        	}
        	if(result.hasExceptions() && getLog().isDebugEnabled()) {
            	for(Exception exception : result.getExceptions()) {
    		    	getLog().debug(exception);
            	}
        	}
        }
   		final Set<Artifact> dependencies = result.getArtifacts();
		return dependencies;
    }
}
