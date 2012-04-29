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
            runFitNesseServer();
            Thread.currentThread().join();
        } catch (Exception e) {
            throw new MojoExecutionException("Exception launching FitNesse", e);
        }
    }
}
