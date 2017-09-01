package de.mediathekview.mserver.base.config;

/**
 * The basic configs which can be set for all
 * {@link de.mediathekview.mlib.daten.Sender} and can be overwritten for
 * particular one.
 */
public class MServerBasicConfigDTO
{

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
     * <b>Example:</b> If a Sendung overview side has 10 pages with videos for
     * this Sendung and the amount set by this is 5 then the crawler crawls
     * pages 1 to 5.
     */
    private Integer maximumSubpages;

    /**
     * The maximum amount of days going to past will be crawled for the "Sendung
     * Verpasst?" section.
     */
    private Integer maximumDaysForSendungVerpasstSection;

    public MServerBasicConfigDTO()
    {
        super();
        maximumUrlsPerTask = 50;
        maximumCrawlDurationInMinutes = 30;
        maximumSubpages = 3;
        maximumDaysForSendungVerpasstSection = 6;
    }

    public Integer getMaximumUrlsPerTask()
    {
        return maximumUrlsPerTask;
    }

    public void setMaximumUrlsPerTask(final Integer aMaximumUrlsPerTask)
    {
        maximumUrlsPerTask = aMaximumUrlsPerTask;
    }

    public Integer getMaximumCrawlDurationInMinutes()
    {
        return maximumCrawlDurationInMinutes;
    }

    public void setMaximumCrawlDurationInMinutes(final Integer aMaximumCrawlDurationInMinutes)
    {
        maximumCrawlDurationInMinutes = aMaximumCrawlDurationInMinutes;
    }

    public Integer getMaximumSubpages()
    {
        return maximumSubpages;
    }

    public void setMaximumSubpages(final Integer aMaximumSubpages)
    {
        maximumSubpages = aMaximumSubpages;
    }

    public Integer getMaximumDaysForSendungVerpasstSection()
    {
        return maximumDaysForSendungVerpasstSection;
    }

    public void setMaximumDaysForSendungVerpasstSection(final Integer aMaximumDaysForSendungVerpasstSection)
    {
        maximumDaysForSendungVerpasstSection = aMaximumDaysForSendungVerpasstSection;
    }

    @Override
    public boolean equals(final Object aO)
    {
        if (this == aO)
        {
            return true;
        }
        if (aO == null || getClass() != aO.getClass())
        {
            return false;
        }

        final MServerBasicConfigDTO that = (MServerBasicConfigDTO) aO;

        if (getMaximumUrlsPerTask() != null ? !getMaximumUrlsPerTask().equals(that.getMaximumUrlsPerTask())
                : that.getMaximumUrlsPerTask() != null)
        {
            return false;
        }
        if (getMaximumCrawlDurationInMinutes() != null
                ? !getMaximumCrawlDurationInMinutes().equals(that.getMaximumCrawlDurationInMinutes())
                : that.getMaximumCrawlDurationInMinutes() != null)
        {
            return false;
        }
        if (getMaximumSubpages() != null ? !getMaximumSubpages().equals(that.getMaximumSubpages())
                : that.getMaximumSubpages() != null)
        {
            return false;
        }
        return getMaximumDaysForSendungVerpasstSection() != null
                ? getMaximumDaysForSendungVerpasstSection().equals(that.getMaximumDaysForSendungVerpasstSection())
                : that.getMaximumDaysForSendungVerpasstSection() == null;
    }

    @Override
    public int hashCode()
    {
        int result = getMaximumUrlsPerTask() != null ? getMaximumUrlsPerTask().hashCode() : 0;
        result = 31 * result
                + (getMaximumCrawlDurationInMinutes() != null ? getMaximumCrawlDurationInMinutes().hashCode() : 0);
        result = 31 * result + (getMaximumSubpages() != null ? getMaximumSubpages().hashCode() : 0);
        result = 31 * result + (getMaximumDaysForSendungVerpasstSection() != null
                ? getMaximumDaysForSendungVerpasstSection().hashCode()
                : 0);
        return result;
    }
}
