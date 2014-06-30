package uk.co.javahelp.maven.plugin.fitnesse.junit;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.Launch;
import fitnesse.Arguments;
import fitnesse.junit.PrintTestListener;
import fitnesse.responders.run.JavaFormatter;
import fitnesse.responders.run.ResultsListener;
import fitnesse.testsystems.TestSummary;
import fitnesseMain.FitNesseMain;

/**
 * @see fitnesse.junit.TestHelper
 */
public class TestHelper {

	private final String fitNesseRootPath;
	private final String outputPath;
	private final ResultsListener resultListener;

	private boolean debug = true;

	public static final String PAGE_TYPE_SUITE = fitnesse.junit.TestHelper.PAGE_TYPE_SUITE;
	public static final String PAGE_TYPE_TEST = fitnesse.junit.TestHelper.PAGE_TYPE_TEST;

	public TestHelper(String fitNesseRootPath, String outputPath) {
		this(fitNesseRootPath, outputPath, new PrintTestListener());
	}

	public TestHelper(String fitNesseRootPath, String outputPath,
			ResultsListener resultListener) {
		this.fitNesseRootPath = fitNesseRootPath;
		this.outputPath = outputPath;
		this.resultListener = resultListener;
	}

	/*
	public TestSummary runSuite(String suiteName) throws Exception {
		return run(suiteName, PAGE_TYPE_SUITE);
	}

	public TestSummary runSuite(String suiteName, String suiteFilter)
			throws Exception {
		return run(suiteName, PAGE_TYPE_SUITE, suiteFilter);
	}

	public TestSummary runTest(String suiteName) throws Exception {
		return run(suiteName, PAGE_TYPE_TEST);
	}

	public TestSummary run(String pageName, String pageType) throws Exception {
		return run(pageName, pageType, null);
	}

	public TestSummary run(String pageName, String pageType,
			String suiteFilter, int port) throws Exception {
		return run(pageName, pageType, suiteFilter, null, port);
	}
	*/

	public TestSummary run(final Launch launch, final int port)
			throws Exception {
		final String[] pageNameAndType = launch.calcPageNameAndType();
		final String pageName = pageNameAndType[0];
		final String pageType = pageNameAndType[1];
		JavaFormatter testFormatter = JavaFormatter.getInstance(pageName);
		testFormatter
				.setResultsRepository(new JavaFormatter.FolderResultsRepository(
						outputPath));
		testFormatter.setListener(resultListener);
		Arguments arguments = new Arguments();
		arguments.setDaysTillVersionsExpire("0");
		arguments.setInstallOnly(false);
		arguments.setOmitUpdates(true);
		arguments.setPort(String.valueOf(port));
		arguments.setRootPath(fitNesseRootPath);
		arguments.setCommand(getCommand(pageName, pageType,
				launch.getSuiteFilter(),
				launch.getExcludeSuiteFilter()));
		FitNesseMain.dontExitAfterSingleCommand = true;
		FitNesseMain.launchFitNesse(arguments);
		return testFormatter.getTotalSummary();
	}

	/*
	public TestSummary run(String pageName, String pageType, String suiteFilter)
			throws Exception {
		return run(pageName, pageType, suiteFilter, 0);
	}
	*/

	String getCommand(String pageName, String pageType, String suiteFilter,
			String excludeSuiteFilter) {
		String command = pageName + "?" + pageType + getCommandArgs();
		if (suiteFilter != null)
			command = command + "&suiteFilter=" + suiteFilter;
		if (excludeSuiteFilter != null)
			command = command + "&excludeSuiteFilter=" + excludeSuiteFilter;
		return command;
	}

	private static final String COMMON_ARGS = "&nohistory=true&format=java";
	private static final String DEBUG_ARG = "&debug=true";

	private String getCommandArgs() {
		if (debug) {
			return DEBUG_ARG + COMMON_ARGS;
		}
		return COMMON_ARGS;
	}

	public void setDebugMode(boolean enabled) {
		debug = enabled;
	}
}
