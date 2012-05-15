package uk.co.javahelp.maven.plugin.fitnesse;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal wiki
 * @requiresDependencyResolution
 */
public class WikiMojo extends AbstractMojo {

    public void executeInternal() throws MojoExecutionException, MojoFailureException {
        try {
            this.fitNesseHelper.runFitNesseServer(this.port.toString(), this.workingDir, this.root, this.logDir);
    		if(this.createSymLink) {
	            this.fitNesseHelper.createSymLink(this.suite, this.test, this.project.getBasedir(), this.testResourceDirectory, this.port);
    		}
            Thread.currentThread().join();
        } catch (Exception e) {
            throw new MojoExecutionException("Exception launching FitNesse", e);
        }
    }
}
