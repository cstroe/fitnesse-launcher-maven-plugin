package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import static org.codehaus.plexus.util.IOUtil.close;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefireHelper;
import org.apache.maven.plugin.surefire.SurefireReportParameters;
import org.apache.maven.surefire.failsafe.model.FailsafeSummary;
import org.apache.maven.surefire.failsafe.model.io.xpp3.FailsafeSummaryXpp3Writer;
import org.apache.maven.surefire.suite.RunResult;

import uk.co.javahelp.maven.plugin.fitnesse.responders.run.DelegatingResultsListener;
import fitnesse.junit.JUnitXMLTestListener;
import fitnesse.junit.PrintTestListener;
import fitnesse.junit.TestHelper;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;

/**
 * Goal that runs FitNesse tests using fitnesse.junit.TestHelper.
 * Intended to be bound to the 'integration-test' phase.
 * 
 * @goal run-tests
 * @phase integration-test
 * @requiresDependencyResolution runtime
 * @see fitnesse.junit.TestHelper
 */
public class RunTestsMojo extends AbstractFitNesseMojo implements SurefireReportParameters {

	@Override
    public void executeInternal() throws MojoExecutionException, MojoFailureException {

		if (this.createSymLink) {
			createSymLink();
		}

		final TestSummary fitNesseSummary = runFitNesseTests();
		getLog().info(fitNesseSummary.toString());
		final FailsafeSummary failsafeSummary = convertAndReportResults(fitNesseSummary);
		writeSummary(failsafeSummary);
	}

	/**
	 * Creating a SymLink is easiest when FitNesse is running in 'wiki server' mode.
	 */
	private void createSymLink() throws MojoExecutionException {
		final String portString = this.port.toString();
		try {
            this.fitNesseHelper.launchFitNesseServer(portString, this.workingDir, this.root, this.logDir);
			this.fitNesseHelper.createSymLink(this.suite, this.test, this.project.getBasedir(), this.testResourceDirectory, this.port);
		} catch (Exception e) {
			throw new MojoExecutionException("Exception creating FitNesse SymLink", e);
		} finally {
			this.fitNesseHelper.shutdownFitNesseServer(portString);
		}
	}

	/**
     * Strange side-effect behaviour:
     * If debug=false, FitNesse falls into wiki mode.
	 */
	private TestSummary runFitNesseTests() throws MojoExecutionException {
		final ResultsListener resultsListener = new DelegatingResultsListener(
                new PrintTestListener(), new JUnitXMLTestListener( this.resultsDir.getAbsolutePath()));
        final TestHelper helper = new TestHelper(this.workingDir, this.reportsDir.getAbsolutePath(), resultsListener);
		helper.setDebugMode(true);

		// ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		// org.codehaus.plexus.classworlds.realm.ClassRealm oldLookupRealm = container.setLookupRealm( this.realm );
		try {
			final String[] pageNameAndType = this.fitNesseHelper.calcPageNameAndType(this.suite, this.test);
			// Thread.currentThread().setContextClassLoader( this.realm );
			final TestSummary summary = helper.run(pageNameAndType[0], pageNameAndType[1], this.suiteFilter, this.excludeSuiteFilter, this.port);
			return summary;
		} catch (Exception e) {
			throw new MojoExecutionException("Exception running FitNesse tests", e);
		} finally {
			// Thread.currentThread().setContextClassLoader( oldClassLoader );
			// container.setLookupRealm( oldLookupRealm );
		}
	}

    private FailsafeSummary convertAndReportResults(final TestSummary summary) throws MojoFailureException {
        final RunResult result = new RunResult(summary.right, summary.exceptions, summary.wrong, summary.ignores);
		SurefireHelper.reportExecution(this, result, getLog());
		final FailsafeSummary failsafeSummary = new FailsafeSummary();
		failsafeSummary.setResult(result.getForkedProcessCode());
		return failsafeSummary;
	}

    private void writeSummary(final FailsafeSummary summary) throws MojoExecutionException {
		Writer writer = null;
		try {
			writer = new FileWriter(this.summaryFile);
			FailsafeSummaryXpp3Writer xpp3Writer = new FailsafeSummaryXpp3Writer();
			xpp3Writer.write(writer, summary);
		} catch (IOException e) {
            throw new MojoExecutionException("Exception writing Failsafe summary", e);
		} finally {
			close(writer);
		}
	}

	// ------------------------------------------------------------------------
	// See http://maven.apache.org/plugins/maven-surefire-plugin/test-mojo.html
	// ------------------------------------------------------------------------

	@Override
	public boolean isSkipTests() {
		return false;
	}

	@Override
    public void setSkipTests(boolean skipTests) {}

	@Override
	public boolean isSkipExec() {
		return false;
	}

	@Override
    public void setSkipExec(boolean skipExec) {}

	@Override
	public boolean isSkip() {
		return false;
	}

	@Override
    public void setSkip(boolean skip) {}

	@Override
	public boolean isTestFailureIgnore() {
		return true;
	}

	@Override
    public void setTestFailureIgnore(boolean testFailureIgnore) {}

	@Override
	public File getBasedir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBasedir(File basedir) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getTestClassesDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTestClassesDirectory(File testClassesDirectory) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getReportsDirectory() {
		return this.reportsDir;
	}

	@Override
	public void setReportsDirectory(File reportsDirectory) {
		this.reportsDir = reportsDirectory;
	}

	@Override
	public Boolean getFailIfNoTests() {
		return this.failIfNoTests;
	}

	@Override
	public void setFailIfNoTests(final Boolean failIfNoTests) {
		this.failIfNoTests = failIfNoTests;
	}
}
