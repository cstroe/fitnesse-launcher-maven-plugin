package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefireHelper;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.ReaderFactory;
import org.apache.maven.surefire.suite.RunResult;
import org.codehaus.plexus.util.IOUtil;

/**
 * Goal that provides summary report on FitNesse tests run with 'run-tests'
 * goal. Intended to be bound to the 'verify' phase. Will fail the build if
 * there are test failures.
 * 
 * @goal verify
 * @phase verify
 */
public class VerifyMojo extends RunTestsMojo {

	public VerifyMojo() {
		super(false);
	}

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		final RunResult summary = readSummary();
		SurefireHelper.reportExecution(this, summary, getLog());
	}

	/**
	 * @see org.apache.maven.plugin.failsafe.VerifyMojo
	 */
	private RunResult readSummary() throws MojoExecutionException {
		FileInputStream fileInputStream = null;
		BufferedInputStream bufferedInputStream = null;
		try {
			fileInputStream = new FileInputStream(this.summaryFile);
			bufferedInputStream = new BufferedInputStream(fileInputStream);
			return RunResult.fromInputStream(bufferedInputStream, ReaderFactory.UTF_8);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} finally {
			IOUtil.close(bufferedInputStream);
			IOUtil.close(fileInputStream);
		}
	}
}
