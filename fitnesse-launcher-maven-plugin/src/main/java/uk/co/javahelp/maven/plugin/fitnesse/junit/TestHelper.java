package uk.co.javahelp.maven.plugin.fitnesse.junit;

import uk.co.javahelp.maven.plugin.fitnesse.main.FitNesseMain;
import uk.co.javahelp.maven.plugin.fitnesse.mojo.Launch;
import fitnesse.Arguments;
import fitnesse.reporting.JavaFormatter;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;

/**
 * @see fitnesse.junit.TestHelper
 */
public class TestHelper {

	private final String fitNesseRootPath;

	private final String outputPath;

	private final TestSystemListener<WikiTestPage> resultListener;

	private boolean debug = true;

	public TestHelper(
			final String fitNesseRootPath,
			final String outputPath,
			final TestSystemListener<WikiTestPage> resultListener) {
		this.fitNesseRootPath = fitNesseRootPath;
		this.outputPath = outputPath;
		this.resultListener = resultListener;
	}

	public TestSummary run(final int port, final Launch... launches)
			throws Exception {
		final TestSummary global = new TestSummary();
		for (final Launch launch : launches) {
			global.add(run(launch, port));
		}
		return global;
	}

	public TestSummary run(final Launch launch, final int port) throws Exception {
		JavaFormatter testFormatter = JavaFormatter.getInstance(launch.getPageName());
		testFormatter.setResultsRepository(
						new JavaFormatter.FolderResultsRepository(this.outputPath));
		testFormatter.setListener(resultListener);
		Arguments arguments = new Arguments();
		arguments.setDaysTillVersionsExpire("0");
		arguments.setInstallOnly(false);
		arguments.setOmitUpdates(true);
		arguments.setPort(String.valueOf(port));
		arguments.setRootPath(this.fitNesseRootPath);
		arguments.setCommand(launch.getCommand(this.debug));
		new FitNesseMain().launchFitNesse(arguments);
		return testFormatter.getTotalSummary();
	}

	public void setDebugMode(final boolean enabled) {
		debug = enabled;
	}
}
