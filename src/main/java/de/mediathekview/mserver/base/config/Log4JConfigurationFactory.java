package de.mediathekview.mserver.base.config;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

/**
 * A programmatic Log4J configuration which uses the configuration of
 * {@link MServerLogSettingsDTO}
 * 
 * @author nicklas
 *
 */
@Plugin(name = "Log4JConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(1)
public class Log4JConfigurationFactory extends ConfigurationFactory {
	private static final String[] SUPPORTED_TYPES = new String[] { "*" };
	private static final String ATTRIBUTE_FILE_NAME = "fileName";
	private static final String ATTRIBUTE_FILE_PATTERN = "filePattern";
	private static final String APPENDER_FILE = "File";
	private static final String APPENDER_ROLLING_FILE = "RollingFile";
	private static final String COMPONENT_ON_STARTUP_TRIGGERING_POLICY = "OnStartupTriggeringPolicy";
	private static final String COMPONENT_POLICIES = "Policies";
	private static final String APPENDER_NAME_STDERR = "Stderr";
	private static final String APPENDER_NAME_STDOUT = "Stdout";
	private static final String APPENDER_NAME_FILE = "file";
	private static final String APPENDER_NAME_ROLLING_FILE = "rollingfile";
	private static final String LAYOUT_PATTERN = "PatternLayout";
	private static final String FILTER_THRESHOLD = "ThresholdFilter";
	private static final String ATTRIBUTE_LEVEL = "level";
	private static final String ATTRIBUTE_PATTERN = "pattern";
	private static final String ATTRIBUTE_TARGET = "target";
	private static final String CONSOLE = "CONSOLE";

	private static MServerLogSettingsDTO logSettings;

	static Configuration createConfiguration(final String name,
			final ConfigurationBuilder<BuiltConfiguration> aBuilder) {
		logSettings = MServerConfigManager.getInstance().getConfig().getLogSettings();

		aBuilder.setConfigurationName(name);

		RootLoggerComponentBuilder rootLogger = aBuilder.newRootLogger(logSettings.getLogLevelConsole());
		if (logSettings.getLogActivateConsole()) {
			addConsoleOutBuilder(aBuilder);
			addConsoleErrBuilder(aBuilder);
			rootLogger.add(aBuilder.newAppenderRef(APPENDER_NAME_STDOUT));
			rootLogger.add(aBuilder.newAppenderRef(APPENDER_NAME_STDERR));
		}

		if (logSettings.getLogActivateFile()) {
			addFileBuilder(aBuilder);
			if (logSettings.getLogActivateRollingFileAppend()) {
				rootLogger.add(aBuilder.newAppenderRef(APPENDER_NAME_ROLLING_FILE));
			} else {
				rootLogger.add(aBuilder.newAppenderRef(APPENDER_NAME_FILE));
			}

		}

		aBuilder.add(rootLogger);
		return aBuilder.build();
	}

	private static void addFileBuilder(final ConfigurationBuilder<BuiltConfiguration> aBuilder) {
		ComponentBuilder<?> triggeringPolicy = aBuilder.newComponent(COMPONENT_POLICIES)
				.addComponent(aBuilder.newComponent(COMPONENT_ON_STARTUP_TRIGGERING_POLICY));

		AppenderComponentBuilder appenderBuilder;

		if (logSettings.getLogActivateRollingFileAppend()) {
			appenderBuilder = aBuilder.newAppender(APPENDER_NAME_ROLLING_FILE, APPENDER_ROLLING_FILE);

			appenderBuilder.addAttribute(ATTRIBUTE_FILE_PATTERN, logSettings.getLogFileRollingPattern())
					.addComponent(triggeringPolicy);
		} else {
			appenderBuilder = aBuilder.newAppender(APPENDER_NAME_FILE, APPENDER_FILE);
		}

		appenderBuilder.addAttribute(ATTRIBUTE_FILE_NAME, logSettings.getLogFileSavePath());

		addPattern(aBuilder, appenderBuilder, logSettings.getLogPatternFile());

		appenderBuilder.add(aBuilder.newFilter(FILTER_THRESHOLD, Filter.Result.NEUTRAL, Filter.Result.DENY)
				.addAttribute(ATTRIBUTE_LEVEL, logSettings.getLogLevelFile()));

		aBuilder.add(appenderBuilder);

	}

	private static void addConsoleOutBuilder(final ConfigurationBuilder<BuiltConfiguration> aBuilder) {
		final AppenderComponentBuilder consoleOutAppenderBuilder = aBuilder.newAppender(APPENDER_NAME_STDOUT, CONSOLE)
				.addAttribute(ATTRIBUTE_TARGET, ConsoleAppender.Target.SYSTEM_OUT);

		addPattern(aBuilder, consoleOutAppenderBuilder, logSettings.getLogPatternConsole());
		consoleOutAppenderBuilder.add(aBuilder.newFilter(FILTER_THRESHOLD, Filter.Result.DENY, Filter.Result.NEUTRAL)
				.addAttribute(ATTRIBUTE_LEVEL, Level.ERROR));

		aBuilder.add(consoleOutAppenderBuilder);
	}

	private static void addConsoleErrBuilder(final ConfigurationBuilder<BuiltConfiguration> aBuilder) {
		final AppenderComponentBuilder consoleErrAppenderBuilder = aBuilder.newAppender(APPENDER_NAME_STDERR, CONSOLE)
				.addAttribute(ATTRIBUTE_TARGET, ConsoleAppender.Target.SYSTEM_ERR);

		addPattern(aBuilder, consoleErrAppenderBuilder, logSettings.getLogPatternConsole());

		consoleErrAppenderBuilder.add(aBuilder.newFilter(FILTER_THRESHOLD, Filter.Result.ACCEPT, Filter.Result.DENY)
				.addAttribute(ATTRIBUTE_LEVEL, Level.ERROR));

		aBuilder.add(consoleErrAppenderBuilder);
	}

	private static void addPattern(final ConfigurationBuilder<BuiltConfiguration> aBuilder,
			final AppenderComponentBuilder aAppenderBuilder, String aPattern) {
		aAppenderBuilder.add(aBuilder.newLayout(LAYOUT_PATTERN).addAttribute(ATTRIBUTE_PATTERN, aPattern));
	}

	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
		return getConfiguration(loggerContext, source.toString(), null);
	}

	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final String name,
			final URI configLocation) {
		final ConfigurationBuilder<BuiltConfiguration> aBuilder = newConfigurationBuilder();
		return createConfiguration(name, aBuilder);
	}

	@Override
	protected String[] getSupportedTypes() {
		return SUPPORTED_TYPES;
	}
}
