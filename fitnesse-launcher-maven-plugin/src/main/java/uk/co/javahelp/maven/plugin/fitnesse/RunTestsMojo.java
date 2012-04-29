package uk.co.javahelp.maven.plugin.fitnesse;

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
import fitnesse.Shutdown;
import fitnesse.junit.JUnitXMLTestListener;
import fitnesse.junit.PrintTestListener;
import fitnesse.junit.TestHelper;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;

/**
 * @goal run-tests
 * @requiresDependencyResolution
 */
public class RunTestsMojo extends AbstractMojo implements
        SurefireReportParameters {


    public void executeInternal() throws MojoExecutionException, MojoFailureException {
        final ResultsListener resultsListener = new DelegatingResultsListener(
                new PrintTestListener(), new JUnitXMLTestListener( this.resultsDir.getAbsolutePath()));
        final TestHelper helper = new TestHelper(this.workingDir, this.reportsDir.getAbsolutePath(), resultsListener);
        // Strange side-effect behaviour:
        // If debug=false, FitNesse falls into wiki mode
        helper.setDebugMode(true);

        try {
            // Creating a SymLink is easiest when FitNesse is running in 'wiki server' mode
    		if(this.createSymLink) {
	            runFitNesseServer(); // this will create the SymLink for us
                Shutdown.main(new String[]{"-p", this.port.toString()});
			}

            final String[] pageNameAndType = calcPageNameAndType();
            final TestSummary summary = helper.run(pageNameAndType[0], pageNameAndType[1], this.suiteFilter, this.excludeSuiteFilter, this.port);
            getLog().info(summary.toString());
            final RunResult result = new RunResult(summary.right, summary.exceptions, summary.wrong, summary.ignores);
            SurefireHelper.reportExecution(this, result, getLog());
            final FailsafeSummary failsafeSummary = new FailsafeSummary();
            failsafeSummary.setResult(result.getForkedProcessCode());
            writeSummary(failsafeSummary);
        } catch (Exception e) {
            throw new MojoExecutionException("Exception running FitNesse", e);
        }
    }

    private void writeSummary(FailsafeSummary summary)
            throws MojoExecutionException {
        if (!summaryFile.getParentFile().isDirectory()) {
            summaryFile.getParentFile().mkdirs();
        }

        Writer writer = null;
        try {
            writer = new FileWriter(this.summaryFile);
            FailsafeSummaryXpp3Writer xpp3Writer = new FailsafeSummaryXpp3Writer();
            xpp3Writer.write(writer, summary);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            close(writer);
        }
    }

    // --------------------------------------------------------------------

    public boolean isSkipTests() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSkipTests(boolean skipTests) {
        // TODO Auto-generated method stub

    }

    public boolean isSkipExec() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSkipExec(boolean skipExec) {
        // TODO Auto-generated method stub

    }

    public boolean isSkip() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSkip(boolean skip) {
        // TODO Auto-generated method stub

    }

    public boolean isTestFailureIgnore() {
        return true;
    }

    public void setTestFailureIgnore(boolean testFailureIgnore) {
        // TODO Auto-generated method stub

    }

    public File getBasedir() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setBasedir(File basedir) {
        // TODO Auto-generated method stub

    }

    public File getTestClassesDirectory() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setTestClassesDirectory(File testClassesDirectory) {
        // TODO Auto-generated method stub

    }

    public File getReportsDirectory() {
        return this.reportsDir;
    }

    public void setReportsDirectory(File reportsDirectory) {
        // TODO Auto-generated method stub

    }

    public Boolean getFailIfNoTests() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFailIfNoTests(Boolean failIfNoTests) {
        // TODO Auto-generated method stub

    }
}
