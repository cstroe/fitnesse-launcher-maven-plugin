package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.surefire.SurefireHelper;
import org.apache.maven.surefire.failsafe.model.FailsafeSummary;
import org.apache.maven.surefire.failsafe.model.io.xpp3.FailsafeSummaryXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @goal verify
 */
public class VerifyMojo extends RunTestsMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        final FailsafeSummary summary = readSummary();
        final int result = summary.getResult();
        SurefireHelper.reportExecution(this, result, getLog());
    }

    private FailsafeSummary readSummary() throws MojoExecutionException {

        Reader reader = null;
        try {
            reader = new FileReader(this.summaryFile);
            final FailsafeSummaryXpp3Reader xpp3Reader = new FailsafeSummaryXpp3Reader();
            return xpp3Reader.read(reader);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (XmlPullParserException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            IOUtil.close(reader);
        }
    }

    @Override
    public boolean isTestFailureIgnore() {
        return false;
    }
}
