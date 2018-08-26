package de.mediathekview.mserver.base.config;

/**
 * The basic configs which can be set for all {@link de.mediathekview.mlib.daten.Sender} and can be
 * overwritten for particular one.
 */
public class MServerBasicConfigDTO {

  /**
   * The maximum amount of URLs to be processed per task.
   */
  private Integer maximumUrlsPerTask;

  /**
   * The maximum duration in minutes a crawler may run.
   */
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
  private final Integer maximumDaysForSendungVerpasstSectionFuture;
  
  /**
   * The time in seconds before a socket connection should time out.
   */
  private final Integer socketTimeoutInSeconds;

  public MServerBasicConfigDTO() {
    super();
    maximumUrlsPerTask = 50;
    maximumCrawlDurationInMinutes = 30;
    maximumSubpages = 3;
    maximumDaysForSendungVerpasstSection = 6;
    maximumDaysForSendungVerpasstSectionFuture = 3;
    socketTimeoutInSeconds=60;
  }



  public Integer getMaximumCrawlDurationInMinutes() {
    return maximumCrawlDurationInMinutes;
  }

  public Integer getMaximumDaysForSendungVerpasstSection() {
    return maximumDaysForSendungVerpasstSection;
  }

  public Integer getMaximumDaysForSendungVerpasstSectionFuture() {
    return maximumDaysForSendungVerpasstSectionFuture;
  }

  public Integer getMaximumSubpages() {
    return maximumSubpages;
  }

  public Integer getMaximumUrlsPerTask() {
    return maximumUrlsPerTask;
  }

  @Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((maximumCrawlDurationInMinutes == null) ? 0 : maximumCrawlDurationInMinutes.hashCode());
	result = prime * result
			+ ((maximumDaysForSendungVerpasstSection == null) ? 0 : maximumDaysForSendungVerpasstSection.hashCode());
	result = prime * result + ((maximumDaysForSendungVerpasstSectionFuture == null) ? 0
			: maximumDaysForSendungVerpasstSectionFuture.hashCode());
	result = prime * result + ((maximumSubpages == null) ? 0 : maximumSubpages.hashCode());
	result = prime * result + ((maximumUrlsPerTask == null) ? 0 : maximumUrlsPerTask.hashCode());
	result = prime * result + ((socketTimeoutInSeconds == null) ? 0 : socketTimeoutInSeconds.hashCode());
	return result;
}



@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (!(obj instanceof MServerBasicConfigDTO))
		return false;
	MServerBasicConfigDTO other = (MServerBasicConfigDTO) obj;
	if (maximumCrawlDurationInMinutes == null) {
		if (other.maximumCrawlDurationInMinutes != null)
			return false;
	} else if (!maximumCrawlDurationInMinutes.equals(other.maximumCrawlDurationInMinutes))
		return false;
	if (maximumDaysForSendungVerpasstSection == null) {
		if (other.maximumDaysForSendungVerpasstSection != null)
			return false;
	} else if (!maximumDaysForSendungVerpasstSection.equals(other.maximumDaysForSendungVerpasstSection))
		return false;
	if (maximumDaysForSendungVerpasstSectionFuture == null) {
		if (other.maximumDaysForSendungVerpasstSectionFuture != null)
			return false;
	} else if (!maximumDaysForSendungVerpasstSectionFuture.equals(other.maximumDaysForSendungVerpasstSectionFuture))
		return false;
	if (maximumSubpages == null) {
		if (other.maximumSubpages != null)
			return false;
	} else if (!maximumSubpages.equals(other.maximumSubpages))
		return false;
	if (maximumUrlsPerTask == null) {
		if (other.maximumUrlsPerTask != null)
			return false;
	} else if (!maximumUrlsPerTask.equals(other.maximumUrlsPerTask))
		return false;
	if (socketTimeoutInSeconds == null) {
		if (other.socketTimeoutInSeconds != null)
			return false;
	} else if (!socketTimeoutInSeconds.equals(other.socketTimeoutInSeconds))
		return false;
	return true;
}



public Integer getSocketTimeoutInSeconds() {
	return socketTimeoutInSeconds;
}



public void setMaximumCrawlDurationInMinutes(final Integer aMaximumCrawlDurationInMinutes) {
    maximumCrawlDurationInMinutes = aMaximumCrawlDurationInMinutes;
  }

  public void setMaximumDaysForSendungVerpasstSection(
      final Integer aMaximumDaysForSendungVerpasstSection) {
    maximumDaysForSendungVerpasstSection = aMaximumDaysForSendungVerpasstSection;
  }


  public void setMaximumSubpages(final Integer aMaximumSubpages) {
    maximumSubpages = aMaximumSubpages;
  }


  public void setMaximumUrlsPerTask(final Integer aMaximumUrlsPerTask) {
    maximumUrlsPerTask = aMaximumUrlsPerTask;
  }
}
