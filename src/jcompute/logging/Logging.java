package jcompute.logging;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
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
		// TODO allow values to be configured
		final String MAX_LOGSIZE_BEFORE_ROLLOVER = "10M";
		final String ROLLOVERS_TO_KEEP = "10";
		
		// Use Asynchronous Logging
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		
		final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		
		// The configuration name
		builder.setConfigurationName("jComputeLogConfig");
		
		// Pattern Layout - used for all appenders
		final LayoutComponentBuilder jComputeLogLayout = builder.newLayout("PatternLayout").addAttribute("pattern",
		"%d{yyy-MM-dd HH:mm:ss.SSS} | %r | %5p [%t] - %msg %n");
		
		// Console Appender
		final AppenderComponentBuilder consoleStdIOAppender = builder.newAppender("standardIO", "CONSOLE").addAttribute("target",
		ConsoleAppender.Target.SYSTEM_OUT);
		consoleStdIOAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.DENY, Filter.Result.ACCEPT).addAttribute("level", Level.WARN));
		// Add Console Layout Pattern
		consoleStdIOAppender.add(jComputeLogLayout);
		// Add Console Appender
		builder.add(consoleStdIOAppender);
		
		// Console Error Appender
		final AppenderComponentBuilder consoleStdErrAppender = builder.newAppender("standardERROR", "CONSOLE").addAttribute("target",
		ConsoleAppender.Target.SYSTEM_ERR);
		consoleStdErrAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.DENY).addAttribute("level", Level.WARN));
		// Add Console Layout Pattern
		consoleStdErrAppender.add(jComputeLogLayout);
		// Add Console Error Appender
		builder.add(consoleStdErrAppender);
		
		// Standard Log Appender - Name = filename
		final AppenderComponentBuilder standardAppender = builder.newAppender(standardLogFile, "RollingFile");
		
		final String standardLogName = logDir + File.separatorChar + standardLogFile;
		standardLogPath = standardLogName + ".log";
		final String standardLogRolloverPath = standardLogName + "-%i.log";
		
		standardAppender.addAttribute("fileName", standardLogPath);
		standardAppender.addAttribute("filePattern", standardLogRolloverPath);
		standardAppender.addAttribute("append", "true");
		// Buffered
		standardAppender.addAttribute("immediateFlush", "false");
		standardAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.DENY).addAttribute("level", logLevel));
		
		// Rollover triggers
		final ComponentBuilder<?> standardAppenderTriggeringPolicy = builder.newComponent("Policies");
		standardAppenderTriggeringPolicy.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", MAX_LOGSIZE_BEFORE_ROLLOVER));
		standardAppenderTriggeringPolicy.addComponent(builder.newComponent("OnStartupTriggeringPolicy"));
		
		// Max Rollovers and Strategy
		final ComponentBuilder<?> standardAppenderRolloverStrategy = builder.newComponent("DefaultRolloverStrategy").addAttribute("max", ROLLOVERS_TO_KEEP)
		.addAttribute("fileIndex", "min");
		
		standardAppender.add(jComputeLogLayout);
		standardAppender.addComponent(standardAppenderTriggeringPolicy);
		standardAppender.addComponent(standardAppenderRolloverStrategy);
		
		builder.add(standardAppender);
		
		// Warnings and Errors Log Appender - Name = filename
		final AppenderComponentBuilder errorAppender = builder.newAppender(errorLogFile, "RollingFile");
		
		final String errorLogName = logDir + File.separatorChar + errorLogFile;
		errorLogPath = errorLogName + ".log";
		final String errorLogRolloverPath = errorLogName + "-%i.log";
		
		errorAppender.addAttribute("fileName", errorLogPath);
		errorAppender.addAttribute("filePattern", errorLogRolloverPath);
		
		errorAppender.addAttribute("append", "true");
		// Immediate Flush
		errorAppender.addAttribute("immediateFlush", "true");
		
		errorAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.DENY).addAttribute("level", Level.WARN));
		
		// Rollover triggers
		final ComponentBuilder<?> errorAppenderTriggeringPolicy = builder.newComponent("Policies");
		errorAppenderTriggeringPolicy.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", MAX_LOGSIZE_BEFORE_ROLLOVER));
		errorAppenderTriggeringPolicy.addComponent(builder.newComponent("OnStartupTriggeringPolicy"));
		
		// Max Rollovers and Strategy
		final ComponentBuilder<?> errorAppenderRolloverStrategy = builder.newComponent("DefaultRolloverStrategy").addAttribute("max", ROLLOVERS_TO_KEEP)
		.addAttribute("fileIndex", "min");
		
		errorAppender.add(jComputeLogLayout);
		errorAppender.addComponent(errorAppenderTriggeringPolicy);
		errorAppender.addComponent(errorAppenderRolloverStrategy);
		
		builder.add(errorAppender);
		
		// Root Logger
		final RootLoggerComponentBuilder rootLogger = builder.newRootLogger(logLevel);
		
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
		final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		
		// The configuration name
		builder.setConfigurationName("jComputeTestLoggingConfig");
		
		final LayoutComponentBuilder layout = builder.newLayout("PatternLayout").addAttribute("pattern",
		"%d{yyy-MM-dd HH:mm:ss.SSS} | %r | %5p [%t] - %msg %n");
		final AppenderComponentBuilder consoleStdIOAppender = builder.newAppender("standardIO", "CONSOLE").addAttribute("target",
		ConsoleAppender.Target.SYSTEM_OUT);
		consoleStdIOAppender.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.ACCEPT).addAttribute("level", Level.ALL));
		consoleStdIOAppender.add(layout);
		builder.add(consoleStdIOAppender);
		final RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.ALL);
		rootLogger.add(builder.newAppenderRef("standardIO"));
		builder.add(rootLogger);
		Configurator.initialize(builder.build());
	}
	
	public static String getStandardLogPath()
	{
		return standardLogPath;
	}
}
