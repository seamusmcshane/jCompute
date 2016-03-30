package jCompute.logging;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public final class Logging
{
	private static String standardLogPath;
	private static String errorLogPath;
	
	private Logging()
	{
		
	}

	/**
	 * Programmatic configuration of Log4j2.
	 * Two console loggers with levels filtered to Standard IO or Standard ERROR
	 * Standard IO - Info/Debug
	 * Standard Error - Error/Warn
	 * Two Log Files
	 * Standard - all enabled log levels up to debug
	 * Error - Error and Warn only
	 *
	 * @param logDir
	 * @param standardLogFile
	 * @param errorLogFile
	 * @param logLevel
	 */
	public static void InitLoggingConfig(String logDir, String standardLogFile, String errorLogFile, Level logLevel)
	{
		// Use Asynchronous Logging
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

		// The configuration name
		builder.setConfigurationName("jComputeLogConfig");

		// Pattern Layout - used for all appenders
		LayoutComponentBuilder layout = builder.newLayout("PatternLayout").addAttribute("pattern", "%d{yyy-MM-dd HH:mm:ss.SSS} | %r | %5p [%t] - %msg %n");

		// Console Appender
		AppenderComponentBuilder consoleStdIOAppender = builder.newAppender("standardIO", "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
		consoleStdIOAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.DENY, Filter.Result.ACCEPT).addAttribute("level", Level.WARN));

		// Add Console Layout Pattern
		consoleStdIOAppender.add(layout);

		// Add Console Appender
		builder.add(consoleStdIOAppender);

		// Standard Error
		AppenderComponentBuilder consoleStdErrAppender = builder.newAppender("standardERROR", "CONSOLE").addAttribute("target",
		ConsoleAppender.Target.SYSTEM_ERR);
		consoleStdErrAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.DENY).addAttribute("level", Level.WARN));
		consoleStdErrAppender.add(layout);
		builder.add(consoleStdErrAppender);

		// Standard Log Appender - Name = filename
		AppenderComponentBuilder standardAppender = builder.newAppender(standardLogFile, "File");
		standardAppender.add(layout);
		
		standardLogPath = logDir + File.separatorChar + standardLogFile;
		
		standardAppender.addAttribute("fileName", standardLogPath);
		standardAppender.addAttribute("append", "true");
		// Buffered
		standardAppender.addAttribute("immediateFlush", "false");
		standardAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.DENY).addAttribute("level", logLevel));
		builder.add(standardAppender);

		// Warnings and Errors Log Appender - Name = filename
		AppenderComponentBuilder errorAppender = builder.newAppender(errorLogFile, "File");
		errorAppender.add(layout);
		errorLogPath = logDir + File.separatorChar + errorLogFile;
		errorAppender.addAttribute("fileName", errorLogPath);
		errorAppender.addAttribute("append", "true");
		// Immediate Flush
		errorAppender.addAttribute("immediateFlush", "true");
		errorAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.DENY).addAttribute("level", Level.WARN));
		builder.add(errorAppender);

		// Root Logger
		RootLoggerComponentBuilder rootLogger = builder.newRootLogger(logLevel);

		// Associate Appenders
		rootLogger.add(builder.newAppenderRef("standardIO"));
		rootLogger.add(builder.newAppenderRef("standardERROR"));
		rootLogger.add(builder.newAppenderRef(standardLogFile));
		rootLogger.add(builder.newAppenderRef(errorLogFile));

		rootLogger.addAttribute("additivity", false);

		// Add the root logger
		builder.add(rootLogger);

		// Apply the logging configuration
		Configurator.initialize(builder.build());
	}
	
	public static void initTestLevelLogging()
	{
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		
		// The configuration name
		builder.setConfigurationName("jComputeTestLoggingConfig");

		LayoutComponentBuilder layout = builder.newLayout("PatternLayout").addAttribute("pattern", "%d{yyy-MM-dd HH:mm:ss.SSS} | %r | %5p [%t] - %msg %n");
		AppenderComponentBuilder consoleStdIOAppender = builder.newAppender("standardIO", "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
		consoleStdIOAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.ACCEPT).addAttribute("level", Level.ALL));
		consoleStdIOAppender.add(layout);
		builder.add(consoleStdIOAppender);
		RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.ALL);
		rootLogger.add(builder.newAppenderRef("standardIO"));
		builder.add(rootLogger);
		Configurator.initialize(builder.build());
	}
	
	public static String getStandardLogPath()
	{
		return standardLogPath;
	}
}
