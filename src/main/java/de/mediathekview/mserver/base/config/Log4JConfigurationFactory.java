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
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "Log4JConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(1)
public class Log4JConfigurationFactory extends ConfigurationFactory
{
    private static final String APPENDER_NAME_STDERR = "Stderr";
    private static final String APPENDER_NAME_STDOUT = "Stdout";
    private static final String LAYOUT_PATTERN = "PatternLayout";
    private static final String FILTER_THRESHOLD = "ThresholdFilter";
    private static final String ATTRIBUTE_LEVEL = "level";
    private static final String ATTRIBUTE_PATTERN = "pattern";
    private static final String ATTRIBUTE_TARGET = "target";

    static Configuration createConfiguration(final String name, final ConfigurationBuilder<BuiltConfiguration> builder)
    {
        final MServerLogSettingsDTO logSettings = MServerConfigManager.getInstance().getConfig().getLogSettings();

        builder.setConfigurationName(name);
        builder.setStatusLevel(Level.ERROR);

        if (logSettings.getLogActivateConsole() != null && logSettings.getLogActivateConsole())
        {
            final AppenderComponentBuilder consoleOutAppenderBuilder =
                    builder.newAppender(APPENDER_NAME_STDOUT, "CONSOLE").addAttribute(ATTRIBUTE_TARGET,
                            ConsoleAppender.Target.SYSTEM_OUT);

            addConsolePattern(builder, logSettings, consoleOutAppenderBuilder);

            // consoleOutAppenderBuilder
            // .add(builder.newFilter(FILTER_THRESHOLD, Filter.Result.ACCEPT,
            // Filter.Result.NEUTRAL)
            // .addAttribute(ATTRIBUTE_LEVEL,
            // logSettings.getLogLevelConsole()));
            // consoleOutAppenderBuilder.add(builder.newFilter(FILTER_THRESHOLD,
            // Filter.Result.DENY, Filter.Result.ACCEPT)
            // .addAttribute(ATTRIBUTE_LEVEL, Level.ERROR));

            builder.add(consoleOutAppenderBuilder);

            final AppenderComponentBuilder consoleErrAppenderBuilder =
                    builder.newAppender(APPENDER_NAME_STDERR, "CONSOLE").addAttribute(ATTRIBUTE_TARGET,
                            ConsoleAppender.Target.SYSTEM_ERR);

            addConsolePattern(builder, logSettings, consoleErrAppenderBuilder);

            consoleErrAppenderBuilder
                    .add(builder.newFilter(FILTER_THRESHOLD, Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                            .addAttribute(ATTRIBUTE_LEVEL, logSettings.getLogLevelConsole()));
            // consoleErrAppenderBuilder
            // .add(builder.newFilter(FILTER_THRESHOLD, Filter.Result.ACCEPT,
            // Filter.Result.NEUTRAL)
            // .addAttribute(ATTRIBUTE_LEVEL, Level.ERROR));

            builder.add(consoleErrAppenderBuilder);
        }

        builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
                .add(builder.newAppenderRef(APPENDER_NAME_STDOUT)).addAttribute("additivity", false));
        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef(APPENDER_NAME_STDOUT)));
        return builder.build();
    }

    private static void addConsolePattern(final ConfigurationBuilder<BuiltConfiguration> aBuilder,
            final MServerLogSettingsDTO aLogSettings, final AppenderComponentBuilder aConsoleAppenderBuilder)
    {
        aConsoleAppenderBuilder.add(aBuilder.newLayout(LAYOUT_PATTERN).addAttribute(ATTRIBUTE_PATTERN,
                aLogSettings.getLogPatternConsole()));
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source)
    {
        return getConfiguration(loggerContext, source.toString(), null);
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final String name,
            final URI configLocation)
    {
        final ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
        return createConfiguration(name, builder);
    }

    @Override
    protected String[] getSupportedTypes()
    {
        return new String[]
        { "*" };
    }
}
