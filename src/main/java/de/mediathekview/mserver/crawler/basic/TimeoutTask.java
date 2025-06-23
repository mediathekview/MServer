package de.mediathekview.mserver.crawler.basic;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TimeoutTask extends Thread {
  private static final Logger LOG = LogManager.getLogger(TimeoutTask.class);
  private final long maxTime;
  private boolean isRun;

  protected TimeoutTask(final long aMaxTime) {
    isRun = true;
    maxTime = aMaxTime;
  }

  public void stopTimeout() {
    isRun = false;
  }

  @Override
  public void run() {
    final LocalDateTime beginTime = LocalDateTime.now();
    while (isRun) {
      if (Duration.between(beginTime, LocalDateTime.now()).toMinutes() >= maxTime) {
        shutdown();
        stopTimeout();
      }
      try {Thread.sleep(60*1000L);} catch (InterruptedException e) {
        LOG.error(e);
      }
    }
  }

  public abstract void shutdown();
}
