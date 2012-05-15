package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import uk.co.javahelp.maven.plugin.fitnesse.util.Interrupter;

/**
 * Unfortunately, the FitNesse API does not expose a way to stop the wiki server gracefully.
 * The object / method we need access to is {@link fitnesse.FitNesse.stop()}.
 * The intended use is for the user to press Ctrl+C to quit.
 *
 * @goal wiki
 * @requiresDependencyResolution
 */
public class WikiMojo extends AbstractMojo {

    public void executeInternal() throws MojoExecutionException, MojoFailureException {
        try {
        	Runtime.getRuntime().addShutdownHook(new Interrupter(Thread.currentThread(), 0L));
            this.fitNesseHelper.runFitNesseServer(this.port.toString(), this.workingDir, this.root, this.logDir);
    		if(this.createSymLink) {
	            this.fitNesseHelper.createSymLink(this.suite, this.test, this.project.getBasedir(), this.testResourceDirectory, this.port);
    		}
            Thread.currentThread().join();
        } catch (InterruptedException e) {
        	getLog().info("FitNesse wiki server interrupted!");
        } catch (Exception e) {
            throw new MojoExecutionException("Exception launching FitNesse", e);
        }
    }
}
