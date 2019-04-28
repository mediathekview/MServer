package de.mediathekview.mserver.base.config;

import java.util.Objects;
import java.util.Optional;

/**
 * The basic configs which can be set for all {@link de.mediathekview.mlib.daten.Sender} and can be
 * overwritten for particular one.
 */
public class MServerBasicConfigDTO {
  private final Optional<MServerConfigDTO> parentConfig;
  /** The time in seconds before a socket connection should time out. */
  private Integer socketTimeoutInSeconds;
  /** The maximum amount of URLs to be processed per task. */
  private Integer maximumUrlsPerTask;
  /** The maximum duration in minutes a crawler may run. */
  private Integer maximumCrawlDurationInMinutes;
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

  public MServerBasicConfigDTO() {
    super();

    if (this instanceof MServerConfigDTO) {
      maximumUrlsPerTask = 50;
      maximumCrawlDurationInMinutes = 30;
      maximumSubpages = 3;
      maximumDaysForSendungVerpasstSection = 6;
      maximumDaysForSendungVerpasstSectionFuture = 3;
      socketTimeoutInSeconds = 60;
      parentConfig = Optional.empty();
    } else {
      parentConfig = Optional.ofNullable(MServerConfigManager.getInstance().getConfig());
    }
  }

  public Integer getMaximumCrawlDurationInMinutes() {
    if (maximumCrawlDurationInMinutes == null && parentConfig.isPresent()) {
      return parentConfig.get().getMaximumCrawlDurationInMinutes();
    }
    return maximumCrawlDurationInMinutes;
  }

  public void setMaximumCrawlDurationInMinutes(final Integer aMaximumCrawlDurationInMinutes) {
    maximumCrawlDurationInMinutes = aMaximumCrawlDurationInMinutes;
  }

  public Integer getMaximumDaysForSendungVerpasstSection() {
    if (maximumDaysForSendungVerpasstSection == null && parentConfig.isPresent()) {
      return parentConfig.get().getMaximumDaysForSendungVerpasstSection();
    }
    return maximumDaysForSendungVerpasstSection;
  }

  public void setMaximumDaysForSendungVerpasstSection(
      final Integer aMaximumDaysForSendungVerpasstSection) {
    maximumDaysForSendungVerpasstSection = aMaximumDaysForSendungVerpasstSection;
  }

  public Integer getMaximumDaysForSendungVerpasstSectionFuture() {
    if (maximumDaysForSendungVerpasstSectionFuture == null && parentConfig.isPresent()) {
      return parentConfig.get().getMaximumDaysForSendungVerpasstSectionFuture();
    }
    return maximumDaysForSendungVerpasstSectionFuture;
  }

  public void setMaximumDaysForSendungVerpasstSectionFuture(
      final Integer aMaximumDaysForSendungVerpasstSectionFuture) {
    maximumDaysForSendungVerpasstSectionFuture = aMaximumDaysForSendungVerpasstSectionFuture;
  }

  public Integer getMaximumSubpages() {
    if (maximumSubpages == null && parentConfig.isPresent()) {
      return parentConfig.get().getMaximumSubpages();
    }
    return maximumSubpages;
  }

  public void setMaximumSubpages(final Integer aMaximumSubpages) {
    maximumSubpages = aMaximumSubpages;
  }

  public Integer getMaximumUrlsPerTask() {
    if (maximumUrlsPerTask == null && parentConfig.isPresent()) {
      return parentConfig.get().getMaximumUrlsPerTask();
    }
    return maximumUrlsPerTask;
  }

  public void setMaximumUrlsPerTask(final Integer aMaximumUrlsPerTask) {
    maximumUrlsPerTask = aMaximumUrlsPerTask;
  }

  public Integer getSocketTimeoutInSeconds() {
    if (socketTimeoutInSeconds == null && parentConfig.isPresent()) {
      return parentConfig.get().getSocketTimeoutInSeconds();
    }
    return socketTimeoutInSeconds;
  }

  public void setSocketTimeoutInSeconds(final Integer socketTimeoutInSeconds) {
    this.socketTimeoutInSeconds = socketTimeoutInSeconds;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final MServerBasicConfigDTO that = (MServerBasicConfigDTO) o;
    return Objects.equals(getSocketTimeoutInSeconds(), that.getSocketTimeoutInSeconds())
        && Objects.equals(parentConfig, that.parentConfig)
        && Objects.equals(getMaximumUrlsPerTask(), that.getMaximumUrlsPerTask())
        && Objects.equals(
            getMaximumCrawlDurationInMinutes(), that.getMaximumCrawlDurationInMinutes())
        && Objects.equals(getMaximumSubpages(), that.getMaximumSubpages())
        && Objects.equals(
            getMaximumDaysForSendungVerpasstSection(),
            that.getMaximumDaysForSendungVerpasstSection())
        && Objects.equals(
            getMaximumDaysForSendungVerpasstSectionFuture(),
            that.getMaximumDaysForSendungVerpasstSectionFuture());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getSocketTimeoutInSeconds(),
        parentConfig,
        getMaximumUrlsPerTask(),
        getMaximumCrawlDurationInMinutes(),
        getMaximumSubpages(),
        getMaximumDaysForSendungVerpasstSection(),
        getMaximumDaysForSendungVerpasstSectionFuture());
  }
}
