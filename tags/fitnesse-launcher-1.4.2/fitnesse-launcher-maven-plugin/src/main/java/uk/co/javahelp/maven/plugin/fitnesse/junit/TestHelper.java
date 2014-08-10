package uk.co.javahelp.maven.plugin.fitnesse.junit;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import uk.co.javahelp.maven.plugin.fitnesse.mojo.Launch;
import uk.co.javahelp.maven.plugin.fitnesse.util.FitNesseHelper;
import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.junit.FitNesseSuite;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.PagesByTestSystem;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesseMain.Arguments;

/**
 * @see fitnesse.junit.TestHelper
 */
public class TestHelper {

	private final String fitNesseRootPath;

	private final String outputPath;

	private final TestSystemListener<WikiTestPage> resultListener;

	private boolean debug = true;

	public TestHelper(final String fitNesseRootPath, final String outputPath,
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

	public TestSummary run(final Launch launch, final int port)
			throws Exception {
		FitNesseContext context = FitNesseHelper.initContext(port, this.fitNesseRootPath, FitNesseHelper.DEFAULT_ROOT, null);
		JavaFormatter testFormatter = new JavaFormatter(launch.getPageName());
		testFormatter
				.setResultsRepository(new JavaFormatter.FolderResultsRepository(this.outputPath));
		MultipleTestsRunner testRunner = createTestRunner(
				initChildren(launch.getPageName(), launch.getSuiteFilter(),
						launch.getExcludeSuiteFilter(), context), context);
		testRunner.addTestSystemListener(testFormatter);
		testRunner.addTestSystemListener(resultListener);
		testRunner.executeTestPages();
		return testFormatter.getTotalSummary();
	}

	private List<WikiPage> initChildren(String suiteName, String suiteFilter, String excludeSuiteFilter, FitNesseContext context) {
		WikiPage suiteRoot = getSuiteRootPage(suiteName, context);
		if (!suiteRoot.getData().hasAttribute("Suite")) {
			return Arrays.asList(suiteRoot);
		}
		return new SuiteContentsFinder(suiteRoot,
				new fitnesse.testrunner.SuiteFilter(suiteFilter, excludeSuiteFilter), context.root)
				.getAllPagesToRunForThisSuite();
	}

	private WikiPage getSuiteRootPage(String suiteName, FitNesseContext context) {
		WikiPagePath path = PathParser.parse(suiteName);
		PageCrawler crawler = context.root.getPageCrawler();
		return crawler.getPage(path);
	}

	private MultipleTestsRunner createTestRunner(List<WikiPage> pages, FitNesseContext context) {
		final PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(pages, context.root);

		MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem,
				context.runningTestingTracker, context.testSystemFactory);
		runner.setRunInProcess(debug);
		return runner;
	}

	public void setDebugMode(final boolean enabled) {
		debug = enabled;
	}
}
