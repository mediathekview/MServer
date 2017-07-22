package de.mediathekview.mserver.base.progress.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;

public class ProgressLogMessageListener extends AbstractMessageListener
{
    private static final String PROGRESS_LOGGER_NAME = "MServer Crawler";
    private static final Logger LOG = LogManager.getLogger(PROGRESS_LOGGER_NAME);
    private static final int MIN_SECONDS_BETWEEN_LOGS = 5;
    private LocalDateTime lastLog;

    public ProgressLogMessageListener()
    {
        lastLog = null;
    }

    /**
     * Consumes the progress message and prints it via the logger.
     */
    public void newMessage(String aMessage)
    {
        if (lastLog == null || Duration.between(lastLog, LocalDateTime.now()).getSeconds() >= MIN_SECONDS_BETWEEN_LOGS)
        {
            LOG.info(aMessage);
            lastLog = LocalDateTime.now();
        }
    }
}
