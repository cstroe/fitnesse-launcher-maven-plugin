package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;

public abstract class AbstractSetupsMojo extends org.apache.maven.plugin.AbstractMojo {
    
    /**
     * @parameter expression="${plugin}"
     * @required
     */
    protected PluginDescriptor pluginDescriptor;
    
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
     * @parameter expression="${fitnesse.working}" default-value="${project.build.directory}/fitnesse"
     */
    protected String workingDir;
    
    protected Plugin plugin(final String key) {
       	final Artifact artifact = this.pluginDescriptor.getArtifactMap().get(key);
        final Plugin plugin = new Plugin();
        plugin.setGroupId(artifact.getGroupId());
        plugin.setArtifactId(artifact.getArtifactId());
        plugin.setVersion(artifact.getVersion());
        return plugin;
    }
}
