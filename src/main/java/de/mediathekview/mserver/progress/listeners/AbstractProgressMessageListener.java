package de.mediathekview.mserver.progress.listeners;

import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.MessageUtil;
import de.mediathekview.mserver.base.progress.Progress;
import de.mediathekview.mserver.base.messages.ServerMessages;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.util.Optional;

/** A abstract message listner which consumes crawler progress and generates messages from it. */
public abstract class AbstractProgressMessageListener implements SenderProgressListener {

  @Override
  public void updateProgess(final Sender aSender, final Progress aCrawlerProgress) {
    final Optional<Duration> durationUntilActualDurationExceedsTimeLimit =
        aCrawlerProgress.durationUntilActualDurationExceedsTimeLimit();
    if (durationUntilActualDurationExceedsTimeLimit.isPresent()) {
      newMessage(
          String.format(
              MessageUtil.getInstance()
                  .loadMessageText(ServerMessages.CRAWLER_PROGRESS_WITH_TIME_LIMIT),
              aSender.getName(),
              aCrawlerProgress.calcProgressInPercent(),
              aCrawlerProgress.calcProgressErrorQuoteInPercent(),
              DurationFormatUtils.formatDurationHMS(
                  aCrawlerProgress.durationSinceStart().toMillis()),
              DurationFormatUtils.formatDurationHMS(
                  aCrawlerProgress.calcExpectedTotalDuration().toMillis()),
              DurationFormatUtils.formatDurationHMS(
                  durationUntilActualDurationExceedsTimeLimit.get().toMillis()),
              aCrawlerProgress.getActualCount(),
              aCrawlerProgress.getMaxCount(),
              aCrawlerProgress.getErrorCount(),
              aCrawlerProgress.calcActualErrorQuoteInPercent()));
    } else {
      newMessage(
          String.format(
              MessageUtil.getInstance().loadMessageText(ServerMessages.CRAWLER_PROGRESS),
              aSender.getName(),
              aCrawlerProgress.calcProgressInPercent(),
              aCrawlerProgress.calcProgressErrorQuoteInPercent(),
              DurationFormatUtils.formatDurationHMS(
                  aCrawlerProgress.durationSinceStart().toMillis()),
              DurationFormatUtils.formatDurationHMS(
                  aCrawlerProgress.calcExpectedTotalDuration().toMillis()),
              aCrawlerProgress.getActualCount(),
              aCrawlerProgress.getMaxCount(),
              aCrawlerProgress.getErrorCount(),
              aCrawlerProgress.calcActualErrorQuoteInPercent()));
    }
  }

  abstract void newMessage(String aMessage);
}
