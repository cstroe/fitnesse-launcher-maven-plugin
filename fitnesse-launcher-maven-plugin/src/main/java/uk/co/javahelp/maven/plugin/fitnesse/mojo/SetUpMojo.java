package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * This Mojo is devoted simply to fetching and unpacking
 * FitNesse into a correct working directory structure, and
 * making sure everything is cleanly setup for running FitNesse.
 *
 * @goal set-up
 * @phase pre-integration-test
 */
public class SetUpMojo extends AbstractSetupsMojo {
    
	/**
	 * If set, fitnesse-launcher-maven-plugin will delete any existing plugins.properties
	 * file as part of setting up a clean fitnesse environment.
	 * If you need to retain the plugin.properties file, set this property to false.
	 * 
	 * @parameter property="fitnesse.deletePluginsProperties" default-value="false"
	 */
    protected boolean deletePluginsProperties;
    
	/**
	 * If set, fitnesse-launcher-maven-plugin will always freshly unpack
	 * the FitNesse jar, as part of setting up a clean fitnesse environment.
	 * 
	 * @parameter property="fitnesse.alwaysUnpackFitnesse" default-value="false"
	 */
    protected boolean alwaysUnpackFitnesse;
    
	@Override
    public final void execute() throws MojoExecutionException {
    	if(this.deletePluginsProperties) {
		    clean();
    	}
		unpack();
		move();
    }
    
	/**
	 * <pre>
	 * {@code
	 * <plugin>
	 * 		<artifactId>maven-clean-plugin</artifactId>
	 * 		<executions>
	 * 			<execution>
	 * 				<phase>pre-integration-test</phase>
	 * 				<goals>
	 * 					<goal>clean</goal>
	 * 				</goals>
	 * 				<configuration>
	 * 					<excludeDefaultDirectories>true</excludeDefaultDirectories>
	 * 					<filesets>
	 * 						<fileset>
	 * 							<directory>${fitnesse.working}</directory>
	 * 							<includes>
	 * 								<include>plugins.properties</include>
	 * 							</includes>
	 * 							<followSymlinks>false</followSymlinks>
	 * 						</fileset>
	 * 					</filesets>
	 * 				</configuration>
	 * 			</execution>
	 * 		</executions>
	 * </plugin>
	 * }
	 * </pre>
	 */
    final void clean() throws MojoExecutionException {
		executeMojo(
			plugin("org.apache.maven.plugins:maven-clean-plugin"),
		    goal("clean"),
            configuration(
	        	element("excludeDefaultDirectories", "true"),
	        	element("filesets", 
	            	element("fileset",
		            	element("directory", this.workingDir),
                        element("includes",
   			            	element("include", "plugins.properties")),
		            	element("followSymlinks", "false")))),
		    executionEnvironment(project, session, pluginManager)
		);
    }
    
    /**
	 * <pre>
	 * {@code
	 * <plugin>
	 * 		<artifactId>maven-dependency-plugin</artifactId>
	 * 		<version>2.4</version>
	 * 		<executions>
	 * 			<execution>
	 * 				<phase>pre-integration-test</phase>
	 * 				<goals>
	 * 					<goal>unpack</goal>
	 * 				</goals>
	 * 				<configuration>
	 * 					<artifactItems>
	 * 						<artifactItem>
	 * 							<groupId>org.fitnesse</groupId>
	 * 							<artifactId>fitnesse</artifactId>
	 * 							<version>20130530</version>
	 * 							<type>jar</type>
	 * 							<overWrite>false</overWrite>
	 * 							<outputDirectory>${fitnesse.working}</outputDirectory>
	 * 							<includes>Resources/FitNesseRoot/**</includes>
	 * 						</artifactItem>
	 * 					</artifactItems>
	 * 				</configuration>
	 * 			</execution>
	 * 		</executions>
	 * </plugin>
	 * }
	 * </pre>
     */
    final void unpack() throws MojoExecutionException {
       	final Artifact artifact = this.pluginDescriptor.getArtifactMap().get(FitNesse.artifactKey);
		executeMojo(
			plugin("org.apache.maven.plugins:maven-dependency-plugin"),
		    goal("unpack"),
            configuration(
	        	element("artifactItems",
	            	element("artifactItem",
		            	element("groupId", artifact.getGroupId()),
		            	element("artifactId", artifact.getArtifactId()),
		            	element("version", artifact.getVersion()),
		            	element("type", "jar"),
		            	element("overWrite", Boolean.toString(this.alwaysUnpackFitnesse)),
		            	element("outputDirectory", this.workingDir),
		            	element("includes", "Resources/FitNesseRoot/**")))),
		    executionEnvironment(project, session, pluginManager)
		);
    }
    
    /**
	 * <pre>
	 * {@code
	 * <plugin>
	 *     <groupId>org.apache.maven.plugins</groupId>
	 *     <artifactId>maven-antrun-plugin</artifactId>
	 *     <version>1.7</version>
	 *     <executions>
	 *         <execution>
	 *             <phase>pre-integration-test</phase>
	 *             <goals>
	 *                 <goal>run</goal>
	 *             </goals>
	 *             <configuration>
	 *                 <target>
	 *                     <move file="${fitnesse.working}/Resources/FitNesseRoot"
	 *                          todir="${fitnesse.working}" failonerror="false" />
	 *                 </target>
	 *             </configuration>
	 *         </execution>
	 *     </executions>
	 * </plugin>
	 * }
	 * </pre>
	 */
    final void move() throws MojoExecutionException {
        final Xpp3Dom config = configuration(
        	element("target",
            	element("move")));
        final Xpp3Dom move = config.getChild("target").getChild("move");
        move.setAttribute("file", this.workingDir + "/Resources/FitNesseRoot");
		move.setAttribute("todir", this.workingDir);
		move.setAttribute("failonerror", "false");
		move.setAttribute("overwrite", "false");
		executeMojo(
			plugin("org.apache.maven.plugins:maven-antrun-plugin"),
		    goal("run"),
		    config,
		    executionEnvironment(project, session, pluginManager)
		);
    }
}
