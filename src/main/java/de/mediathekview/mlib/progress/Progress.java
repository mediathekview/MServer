package de.mediathekview.mlib.progress;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/** A POJO to store the progress information. */
public class Progress {
  private final long maxCount;
  private final long actualCount;
  private final long errorCount;
  private final LocalDateTime crawlerStartTime;
  private final Integer maxCrawlerDurationInMinutes;

  public Progress(
      final long maxCount,
      final long actualCount,
      final long errorCount,
      final LocalDateTime crawlerStartTime,
      @Nullable final Integer maxCrawlerDurationInMinutes) {
    super();
    this.maxCount = maxCount;
    this.actualCount = actualCount;
    this.errorCount = errorCount;
    this.crawlerStartTime = crawlerStartTime;
    this.maxCrawlerDurationInMinutes = maxCrawlerDurationInMinutes;
  }

  public long getActualCount() {
    return actualCount;
  }

  public long getErrorCount() {
    return errorCount;
  }

  public long getMaxCount() {
    return maxCount;
  }

  /**
   * Calculates the actual progress in percent.
   *
   * @return The actual progress in percent.
   */
  public float calcProgressInPercent() {
    return maxCount > 0 ? actualCount * 100f / maxCount : 0f;
  }

  /**
   * Calculates the error percentage of actual progress.
   *
   * @return The error percentage of actual progress.
   */
  public float calcActualErrorQuoteInPercent() {
    return actualCount > 0 ? errorCount * 100f / actualCount : 0f;
  }

  /**
   * Calculates the total error percentage.
   *
   * @return The total error percentage.
   */
  public float calcProgressErrorQuoteInPercent() {
    return maxCount > 0 ? errorCount * 100f / maxCount : 0f;
  }

  /** @return The duration since the crawler has started. */
  public Duration durationSinceStart() {
    return Duration.between(crawlerStartTime, LocalDateTime.now());
  }

  /**
   * Calculates the expected total duration of the run.
   *
   * @return The expected total duration of the run.
   */
  public Duration calcExpectedTotalDuration() {
    return Duration.ofNanos(
        Math.round(
            ((double) durationSinceStart().toNanos()) / ((double) calcProgressInPercent()) * 100d));
  }

  public Optional<Duration> durationUntilActualDurationExceedsTimeLimit() {
    if (maxCrawlerDurationInMinutes == null || maxCrawlerDurationInMinutes <= 0) {
      return Optional.empty();
    }
    return Optional.of(
        Duration.ofMinutes(maxCrawlerDurationInMinutes - durationSinceStart().toMinutes()));
  }
}
