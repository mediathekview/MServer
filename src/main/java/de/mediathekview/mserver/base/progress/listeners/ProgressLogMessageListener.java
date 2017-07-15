package de.mediathekview.mserver.base.progress.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;

public class ProgressLogMessageListener extends AbstractMessageListener
{
    private static final String PROGRESS_LOGGER_NAME = "MServer Crawler";
    private static final Logger LOG = LogManager.getLogger(PROGRESS_LOGGER_NAME);
    private LocalDateTime lastLog = LocalDateTime.now();

    /**
     * Consumes the progress message and prints it via the logger.
     */
    public void newMessage(String aMessage)
    {
        System.out.print('\r' + aMessage);
        System.out.flush();
        if (Duration.between(lastLog, LocalDateTime.now()).getSeconds() > 5)
        {
            System.out.print("\n");
            lastLog = LocalDateTime.now();
        }
    }
}
