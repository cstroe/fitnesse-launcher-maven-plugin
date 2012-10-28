package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * See fitnesse.responders.run.formatters.PageInProgressFormatter and 
 * fitnesse.slim.test.TestsInProgress, wherein they have hard-coded the directory 
 * where they track test progress. *sigh*
 *
 * @goal post-clean
 * @phase post-integration-test
 */
public class PostCleanMojo extends AbstractCleanMojo {
    
	@Override
    protected Xpp3Dom cleanConfiguration() {
        return configuration(
			        element(name("excludeDefaultDirectories"), "true"),
			        element(name("filesets"),
			            element(name("fileset"),
    			            element(name("directory"), "FitNesseRoot"),
    			            element(name("followSymlinks"), "false")))
			    );
    }
}
