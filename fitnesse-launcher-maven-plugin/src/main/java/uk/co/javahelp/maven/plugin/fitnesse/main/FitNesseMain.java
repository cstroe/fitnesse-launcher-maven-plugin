package uk.co.javahelp.maven.plugin.fitnesse.main;

import static fitnesse.ConfigurationParameter.COMMAND;
import static fitnesse.ConfigurationParameter.LOG_LEVEL;
import static fitnesse.ConfigurationParameter.OUTPUT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;

import fitnesse.ConfigurationParameter;
import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.components.PluginsClassLoader;
import fitnesseMain.Arguments;

/**
 * Copied from {@link fitnesseMain.FitNesseMain} and altered to throw
 * MojoExecutionException if FitNesse fails to launch.
 */
public class FitNesseMain {
	private static final Logger LOG = Logger.getLogger(FitNesseMain.class
			.getName());

	/*
	public Integer launchFitNesse(Arguments arguments) throws Exception {
		ContextConfigurator contextConfigurator = ContextConfigurator.systemDefaults();
		contextConfigurator = contextConfigurator.updatedWith(System.getProperties());
		contextConfigurator = contextConfigurator
				.updatedWith(ConfigurationParameter.loadProperties(new File(arguments.getConfigFile(contextConfigurator))));
		contextConfigurator = arguments.update(contextConfigurator);

		return launchFitNesse(contextConfigurator);
	}
	*/

	public Integer launchFitNesse(ContextConfigurator contextConfigurator) throws Exception {
		configureLogging("verbose".equalsIgnoreCase(contextConfigurator.get(LOG_LEVEL)));
		loadPlugins();
		FitNesseContext context = contextConfigurator.makeFitNesseContext();
		logStartupInfo(context);
		return launch(context);
	}

	private void loadPlugins() throws Exception {
		new PluginsClassLoader().addPluginsToClassLoader();
	}

	public Integer launch(FitNesseContext context) throws Exception {
		boolean started = context.fitNesse.start();
		if (started) {
			String command = context.getProperty(COMMAND.getKey());
			if (command != null) {
				String output = context.getProperty(OUTPUT.getKey());
				return executeSingleCommand(context.fitNesse, command, output);
			}
		} else {
			throw new MojoExecutionException("FitNesse could not be launched");
		}
		return null;
	}

	private int executeSingleCommand(FitNesse fitNesse, String command, String outputFile) throws Exception {
		LOG.info("Executing command: " + command);

		OutputStream os;

		boolean outputRedirectedToFile = outputFile != null;

		if (outputRedirectedToFile) {
			LOG.info("-----Command Output redirected to " + outputFile + "-----");
			os = new FileOutputStream(outputFile);
		} else {
			LOG.info("-----Command Output-----");
			os = System.out;
		}

		fitNesse.executeSingleCommand(command, os);
		fitNesse.stop();

		if (outputRedirectedToFile) {
			os.close();
		} else {
			LOG.info("-----Command Complete-----");
		}

		return 0;
	}

	private void logStartupInfo(FitNesseContext context) {
		LOG.info("root page: " + context.root);
		LOG.info("logger: " + (context.logger == null ? "none" : context.logger.toString()));
		LOG.info("authenticator: " + context.authenticator);
		LOG.info("page factory: " + context.pageFactory);
		LOG.info("page theme: " + context.pageFactory.getTheme());
		LOG.info("Starting FitNesse on port: " + context.port);
	}

	public void configureLogging(boolean verbose) {
		if (loggingSystemPropertiesDefined()) {
			return;
		}

		InputStream in = FitNesseMain.class
				.getResourceAsStream((verbose ? "verbose-" : "") + "logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(in);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Log configuration failed", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Unable to close Log configuration file", e);
				}
			}
		}
		LOG.finest("Configured verbose logging");
	}

	private boolean loggingSystemPropertiesDefined() {
		return System.getProperty("java.util.logging.config.class") != null
				|| System.getProperty("java.util.logging.config.file") != null;
	}
}
