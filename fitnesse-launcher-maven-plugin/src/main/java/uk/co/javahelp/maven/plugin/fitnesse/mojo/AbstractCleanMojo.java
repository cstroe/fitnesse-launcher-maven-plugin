package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * See fitnesse.responders.run.formatters.PageInProgressFormatter and 
 * fitnesse.slim.test.TestsInProgress, wherein they have hard-coded the directory 
 * where they track test progress. *sigh*
 *
 * @goal clean
 * @phase clean
 */
public abstract class AbstractCleanMojo extends org.apache.maven.plugin.AbstractMojo {
    
    /**
     * Maven project, to be injected by Maven itself.
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;
	
    /**
     * The Maven Session Object
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession session;

    /**
     * The Maven BuildPluginManager Object
     *
     * @component
     * @required
     */
    protected BuildPluginManager pluginManager;
    
    /**
     * @parameter expression="${clean.version}" default-value="2.4.1"
     */
    protected String cleanVersion;
    
    protected Plugin cleanPlugin() {
        final Plugin plugin = new Plugin();
        plugin.setGroupId("org.apache.maven.plugins");
        plugin.setArtifactId("maven-clean-plugin");
        plugin.setVersion(this.cleanVersion);
        return plugin;
    }

    protected abstract Xpp3Dom cleanConfiguration();
    
	@Override
    public void execute() throws MojoExecutionException {
		executeMojo(
		    cleanPlugin(),
		    goal("clean"),
		    cleanConfiguration(),
		    executionEnvironment(project, session, pluginManager)
		);
    }
}
