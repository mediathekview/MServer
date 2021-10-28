package de.mediathekview.mserver.base.config;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The basic configs which can be set for all {@link de.mediathekview.mlib.daten.Sender} and can be
 * overwritten for particular one.
 */
public class MServerBasicConfigDTO {
  @Nullable private MServerConfigDTO parentConfig;
  /** The time in seconds before a socket connection should time out. */
  private Integer socketTimeoutInSeconds;
  /** The maximum amount of URLs to be processed per task. */
  private Integer maximumUrlsPerTask;
  /** The maximum duration in minutes a crawler may run. */
  private Integer maximumCrawlDurationInMinutes;
  /** enables the topics search */
  private Boolean topicsSearchEnabled;
  /**
   * The maximum amount of sub pages to be crawled.<br>
   * <br>
   * <b>Example:</b> If a Sendung overview side has 10 pages with videos for this Sendung and the
   * amount set by this is 5 then the crawler crawls pages 1 to 5.
   */
  private Integer maximumSubpages;
  /**
   * The maximum amount of days going to past will be crawled for the "Sendung Verpasst?" section.
   */
  private Integer maximumDaysForSendungVerpasstSection;
  /**
   * The maximum amount of days going to future will be crawled for the "Sendung Verpasst?" section.
   */
  private Integer maximumDaysForSendungVerpasstSectionFuture;

  /** The maximum amount of requests allowed per second. Only used for a few sender. */
  private Double maximumRequestsPerSecond;

  public MServerBasicConfigDTO(final @Nullable MServerConfigDTO parent) {
    super();
    parentConfig = parent;
  }

  public MServerBasicConfigDTO() {
    this(null);
  }

  public Integer getMaximumCrawlDurationInMinutes() {
    if (maximumCrawlDurationInMinutes == null && parentConfig != null) {
      return parentConfig.getMaximumCrawlDurationInMinutes();
    }
    return maximumCrawlDurationInMinutes;
  }

  public void setMaximumCrawlDurationInMinutes(final Integer aMaximumCrawlDurationInMinutes) {
    maximumCrawlDurationInMinutes = aMaximumCrawlDurationInMinutes;
  }

  public Integer getMaximumDaysForSendungVerpasstSection() {
    if (maximumDaysForSendungVerpasstSection == null && parentConfig != null) {
      return parentConfig.getMaximumDaysForSendungVerpasstSection();
    }
    return maximumDaysForSendungVerpasstSection;
  }

  public void setMaximumDaysForSendungVerpasstSection(
      final Integer aMaximumDaysForSendungVerpasstSection) {
    maximumDaysForSendungVerpasstSection = aMaximumDaysForSendungVerpasstSection;
  }

  public Integer getMaximumDaysForSendungVerpasstSectionFuture() {
    if (maximumDaysForSendungVerpasstSectionFuture == null && parentConfig != null) {
      return parentConfig.getMaximumDaysForSendungVerpasstSectionFuture();
    }
    return maximumDaysForSendungVerpasstSectionFuture;
  }

  public void setMaximumDaysForSendungVerpasstSectionFuture(
      final Integer aMaximumDaysForSendungVerpasstSectionFuture) {
    maximumDaysForSendungVerpasstSectionFuture = aMaximumDaysForSendungVerpasstSectionFuture;
  }

  public Boolean getTopicsSearchEnabled() {
    if (topicsSearchEnabled == null && parentConfig != null) {
      return parentConfig.getTopicsSearchEnabled();
    }
    return topicsSearchEnabled;
  }

  public void setTopicsSearchEnabled(final Boolean topicsSearchEnabled) {
    this.topicsSearchEnabled = topicsSearchEnabled;
  }

  public Integer getMaximumSubpages() {
    if (maximumSubpages == null && parentConfig != null) {
      return parentConfig.getMaximumSubpages();
    }
    return maximumSubpages;
  }

  public void setMaximumSubpages(final Integer aMaximumSubpages) {
    maximumSubpages = aMaximumSubpages;
  }

  public Integer getMaximumUrlsPerTask() {
    if (maximumUrlsPerTask == null && parentConfig != null) {
      return parentConfig.getMaximumUrlsPerTask();
    }
    return maximumUrlsPerTask;
  }

  public void setMaximumUrlsPerTask(final Integer aMaximumUrlsPerTask) {
    maximumUrlsPerTask = aMaximumUrlsPerTask;
  }

  public Integer getSocketTimeoutInSeconds() {
    if (socketTimeoutInSeconds == null && parentConfig != null) {
      return parentConfig.getSocketTimeoutInSeconds();
    }
    return socketTimeoutInSeconds;
  }

  public void setSocketTimeoutInSeconds(final Integer socketTimeoutInSeconds) {
    this.socketTimeoutInSeconds = socketTimeoutInSeconds;
  }

  public Double getMaximumRequestsPerSecond() {
    if (maximumRequestsPerSecond == null && parentConfig != null) {
      return parentConfig.getMaximumRequestsPerSecond();
    }
    return maximumRequestsPerSecond;
  }

  public void setMaximumRequestsPerSecond(final Double maximumRequestsPerSecond) {
    this.maximumRequestsPerSecond = maximumRequestsPerSecond;
  }

  public void setParentConfig(@Nullable final MServerConfigDTO parentConfig) {
    this.parentConfig = parentConfig;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof final MServerBasicConfigDTO that)) {
      return false;
    }
    return Objects.equals(getSocketTimeoutInSeconds(), that.getSocketTimeoutInSeconds())
        && Objects.equals(getMaximumUrlsPerTask(), that.getMaximumUrlsPerTask())
        && Objects.equals(
            getMaximumCrawlDurationInMinutes(), that.getMaximumCrawlDurationInMinutes())
        && Objects.equals(getTopicsSearchEnabled(), that.getTopicsSearchEnabled())
        && Objects.equals(getMaximumSubpages(), that.getMaximumSubpages())
        && Objects.equals(
            getMaximumDaysForSendungVerpasstSection(),
            that.getMaximumDaysForSendungVerpasstSection())
        && Objects.equals(
            getMaximumDaysForSendungVerpasstSectionFuture(),
            that.getMaximumDaysForSendungVerpasstSectionFuture())
        && Objects.equals(getMaximumRequestsPerSecond(), that.getMaximumRequestsPerSecond());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getSocketTimeoutInSeconds(),
        getMaximumUrlsPerTask(),
        getMaximumCrawlDurationInMinutes(),
        getTopicsSearchEnabled(),
        getMaximumSubpages(),
        getMaximumDaysForSendungVerpasstSection(),
        getMaximumDaysForSendungVerpasstSectionFuture(),
        getMaximumRequestsPerSecond());
  }
}
