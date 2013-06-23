package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper.isBlank;

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
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;

public abstract class AbstractFitNesseMojo extends org.apache.maven.plugin.AbstractMojo {

    /**
     * The Maven Session Object
     *
     * @parameter property="session"
     * @readonly
     * @required
     */
    protected MavenSession session;

    /**
     * The Maven BuildPluginManager Object
     *
     * @component
     * @readonly
     * @required
     */
    protected BuildPluginManager pluginManager;
    
    /**
     * Used to look up Artifacts in the remote repository.
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @readonly
     * @required
     */
    protected ArtifactResolver resolver;

    /**
     * Location of the local repository.
     *
     * @parameter property="localRepository"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter property="project.remoteArtifactRepositories"
     * @readonly
     * @required
     */
    protected List<ArtifactRepository> remoteArtifactRepositories;
    
    /**
     * Maven project, to be injected by Maven itself.
     * @parameter property="project"
     * @readonly
     * @required
     */
    protected MavenProject project;
    
    /**
     * @parameter property="plugin"
     * @readonly
     * @required
     */
    protected PluginDescriptor pluginDescriptor;

    /**
     * @parameter property="fitnesse.port" default-value="9123"
     */
    protected Integer port;

    /**
     * @parameter property="fitnesse.test.resource.directory" default-value="src/test/fitnesse"
     */
    protected String testResourceDirectory;

    /**
     * @parameter property="fitnesse.working" default-value="${project.build.directory}/fitnesse"
     */
    protected String workingDir;

    /**
     * @parameter property="fitnesse.root" default-value="FitNesseRoot"
     */
    protected String root;

    /**
     * @parameter property="fitnesse.logDir"
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
     * @parameter property="fitnesse.createSymLink"
     */
    protected boolean createSymLink;

    /**
     * This is where test results go.
     * 
     * @parameter property="fitnesse.results" default-value="${project.build.directory}/fitnesse/results"
     * @required
     */
    protected File resultsDir;

    /**
     * This is where reports go.
     * 
     * @parameter property="fitnesse.reports" default-value="${project.build.directory}/fitnesse/reports"
     * @required
     */
    protected File reportsDir;

    /**
     * The summary file to write integration test results to.
     * 
     * @parameter property="fitnesse.summary.file" default-value="${project.build.directory}/fitnesse/results/failsafe-summary.xml"
     * @required
     */
    protected File summaryFile;

    /**
     * @parameter property="fitnesse.suite"
     */
    protected String suite;

    /**
     * @parameter property="fitnesse.test"
     */
    protected String test;

    /**
     * @parameter property="fitnesse.suiteFilter"
     */
    protected String suiteFilter;

    /**
     * @parameter property="fitnesse.excludeSuiteFilter"
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
        
    protected void exportProperties() throws MojoExecutionException {
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
        if(!isBlank(key) && !isBlank(value)) {
            getLog().info(String.format("Setting FitNesse variable [%s] to [%s]", key, value));
            System.setProperty(key, value);
        }
    }

    protected String calcWikiFormatClasspath() throws MojoExecutionException {
        final Set<Artifact> artifacts = new HashSet<Artifact>();
        
        // We should always have FitNesse itself on the FitNesse classpath!
       	artifacts.addAll(resolveDependencyKey(FitNesse.artifactKey));
                
       	// We check plugin for null to allow use in standalone mode
        final Plugin fitnessePlugin = this.project.getPlugin(this.pluginDescriptor.getPluginLookupKey());
       	if(fitnessePlugin == null) {
            getLog().info("Running standalone - launching vanilla FitNesse");
       	} else {
            final List<Dependency> dependecies = fitnessePlugin.getDependencies();
        	for(Dependency dependency : dependecies) {
        		final String key = dependency.getGroupId() + ":" + dependency.getArtifactId();
        		artifacts.addAll(resolveDependencyKey(key));
        	}
        }
       	
        final StringBuilder wikiFormatClasspath = new StringBuilder("\n");
	    setupLocalTestClasspath(wikiFormatClasspath);
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

	private void setupLocalTestClasspath(final StringBuilder wikiFormatClasspath) throws MojoExecutionException {
		try {
			final List<String> runtimeClasspathElements = this.project.getTestClasspathElements();
			final ClassRealm realm = this.pluginDescriptor.getClassRealm();

			for(final String element : runtimeClasspathElements) {
                this.fitNesseHelper.formatAndAppendClasspath(wikiFormatClasspath, element);
			    final File elementFile = new File(element);
			    realm.addURL(elementFile.toURI().toURL());
			}
		} catch (Exception e) {
            throw new MojoExecutionException("Exception setting up local project test classpath", e);
		}
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
