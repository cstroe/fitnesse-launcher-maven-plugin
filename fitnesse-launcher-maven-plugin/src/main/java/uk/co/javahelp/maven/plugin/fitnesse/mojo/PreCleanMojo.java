package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * See fitnesse.responders.run.formatters.PageInProgressFormatter and 
 * fitnesse.slim.test.TestsInProgress, wherein they have hard-coded the directory 
 * where they track test progress. *sigh*
 *
 * @goal pre-clean
 * @phase pre-integration-test
 */
public class PreCleanMojo extends AbstractCleanMojo {
    
	@Override
    protected Xpp3Dom cleanConfiguration() {
        return configuration(
	        element(name("excludeDefaultDirectories"), "true"),
	        element(name("filesets"),
	            element(name("fileset"),
		            element(name("directory"), "${fitnesse.working}"),
		            element(name("includes"),
   			            element(name("include"), "plugins.properties")),
		            element(name("followSymlinks"), "false")),
	            element(name("fileset"),
		            element(name("directory"), "${project.build.directory}/dependency-maven-plugin-markers"),
		            element(name("includes"),
   			            element(name("include"), "**//*.marker")),
		            element(name("followSymlinks"), "false")))
			    );
    }
}
