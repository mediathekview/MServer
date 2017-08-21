package de.mediathekview.mserver.crawler.basic;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class TimeoutTask extends Thread
{
    private final long maxTime;
    private boolean isRun;

    public TimeoutTask(long aMaxTime)
    {
        isRun = true;
        maxTime = aMaxTime;

    }

    public void stopTimeout()
    {
        isRun = false;
    }

    @Override
    public void run()
    {
        LocalDateTime beginTime = LocalDateTime.now();
        while (isRun)
        {
            if (Duration.between(beginTime, LocalDateTime.now()).toMinutes() > maxTime)
            {
                shutdown();
                stopTimeout();
            }
        }
    }

    public abstract void shutdown();
}