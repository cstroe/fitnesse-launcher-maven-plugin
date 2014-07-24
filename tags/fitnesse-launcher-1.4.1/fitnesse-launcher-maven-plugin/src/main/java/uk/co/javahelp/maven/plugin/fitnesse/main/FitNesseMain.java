package uk.co.javahelp.maven.plugin.fitnesse.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

import fitnesse.Arguments;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContext.Builder;
import fitnesse.PluginsLoader;
import fitnesse.components.ComponentFactory;
import fitnesse.components.PluginsClassLoader;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.VersionsController;
import fitnesse.wiki.fs.ZipFileVersionsController;
import fitnesse.wikitext.parser.SymbolProvider;

/**
 * Copied from {@link fitnesseMain.FitNesseMain} and
 * altered to throw MojoExecutionException if FitNesse fails to launch.
 */
public class FitNesseMain {
	private static String extraOutput;

	public Integer launchFitNesse(Arguments arguments) throws Exception {
		loadPlugins();
		FitNesseContext context = loadContext(arguments);
		return  launch(arguments, context);
	}

	private void loadPlugins() throws Exception {
		new PluginsClassLoader().addPluginsToClassLoader();
	}

	Integer launch(Arguments arguments, FitNesseContext context)
			throws Exception {
		boolean started = context.fitNesse.start();
		if (started) {
			printStartMessage(arguments, context);
			if (arguments.getCommand() != null) {
				return executeSingleCommand(arguments, context);
			}
		} else {
			throw new MojoExecutionException("FitNesse could not be launched");
		}
		return null;
	}

	private int executeSingleCommand(Arguments arguments,
			FitNesseContext context) throws Exception {
		System.out.println("Executing command: " + arguments.getCommand());

		OutputStream os;

		boolean outputRedirectedToFile = arguments.getOutput() != null;

		if (outputRedirectedToFile) {
			System.out.println("-----Command Output redirected to "
					+ arguments.getOutput() + "-----");
			os = new FileOutputStream(arguments.getOutput());
		} else {
			System.out.println("-----Command Output-----");
			os = System.out;
		}

		context.fitNesse.executeSingleCommand(arguments.getCommand(), os);
		context.fitNesse.stop();

		if (outputRedirectedToFile) {
			os.close();
		} else {
			System.out.println("-----Command Complete-----");
		}

		return 0;
	}

	private FitNesseContext loadContext(Arguments arguments) throws Exception {
		Properties properties = loadConfigFile(arguments.getConfigFile());
		// Enrich properties with command line values:
		properties.setProperty(ComponentFactory.VERSIONS_CONTROLLER_DAYS,
				Integer.toString(arguments.getDaysTillVersionsExpire()));

		Builder builder = new Builder();
		ComponentFactory componentFactory = new ComponentFactory(properties);

		WikiPageFactory wikiPageFactory = (WikiPageFactory) componentFactory
				.createComponent(ComponentFactory.WIKI_PAGE_FACTORY_CLASS,
						FileSystemPageFactory.class);

		builder.properties = properties;
		builder.port = arguments.getPort();
		builder.rootPath = arguments.getRootPath();
		builder.rootDirectoryName = arguments.getRootDirectory();

		builder.versionsController = (VersionsController) componentFactory
				.createComponent(ComponentFactory.VERSIONS_CONTROLLER_CLASS,
						ZipFileVersionsController.class);
		builder.versionsController.setHistoryDepth(Integer.parseInt(properties
				.getProperty(ComponentFactory.VERSIONS_CONTROLLER_DAYS, "14")));
		builder.recentChanges = (RecentChanges) componentFactory
				.createComponent(ComponentFactory.RECENT_CHANGES_CLASS,
						RecentChangesWikiPage.class);

		// This should be done before the root wiki page is created:
		// extraOutput =
		// componentFactory.loadVersionsController(arguments.getDaysTillVersionsExpire());

		builder.root = wikiPageFactory.makeRootPage(builder.rootPath,
				builder.rootDirectoryName);

		PluginsLoader pluginsLoader = new PluginsLoader(componentFactory);

		builder.logger = pluginsLoader.makeLogger(arguments.getLogDirectory());
		builder.authenticator = pluginsLoader.makeAuthenticator(arguments
				.getUserpass());

		FitNesseContext context = builder.createFitNesseContext();

		SymbolProvider symbolProvider = SymbolProvider.wikiParsingProvider;

		extraOutput += pluginsLoader.loadPlugins(context.responderFactory,
				symbolProvider);
		extraOutput += pluginsLoader.loadResponders(context.responderFactory);
		extraOutput += pluginsLoader.loadSymbolTypes(symbolProvider);
		extraOutput += pluginsLoader.loadContentFilter();
		extraOutput += pluginsLoader.loadSlimTables();
		extraOutput += pluginsLoader.loadCustomComparators();

		WikiImportTestEventListener.register();

		return context;
	}

	public Properties loadConfigFile(final String propertiesFile) {
		FileInputStream propertiesStream = null;
		Properties properties = new Properties();
		File configurationFile = new File(propertiesFile);
		try {
			propertiesStream = new FileInputStream(configurationFile);
		} catch (FileNotFoundException e) {
			try {
				System.err.println(String.format(
						"No configuration file found (%s)",
						configurationFile.getCanonicalPath()));
			} catch (IOException e1) {
				System.err.println(String.format(
						"No configuration file found (%s)", propertiesFile));
			}
		}

		if (propertiesStream != null) {
			try {
				properties.load(propertiesStream);
				propertiesStream.close();
			} catch (IOException e) {
				System.err.println(String.format(
						"Error reading configuration: %s", e.getMessage()));
			}
		}

		return properties;
	}

	private static void printStartMessage(Arguments args,
			FitNesseContext context) {
		System.out.println("FitNesse (" + context.version + ") Started...");
		System.out.print(context.toString());
		System.out.println("\tpage version expiration set to "
				+ args.getDaysTillVersionsExpire() + " days.");
		if (extraOutput != null)
			System.out.print(extraOutput);
	}
}
