package mServer.crawler.progress;

/**
 * A POJO to store the crawler progress information.
 */
public class CrawlerProgress
{
    private long maxCount;
    private long actualCount;
    private long errorCount;

    public CrawlerProgress(long aMaxCount, long aActualCount, long aErrorCount)
    {
        super();
        maxCount = aMaxCount;
        actualCount = aActualCount;
        errorCount = aErrorCount;
    }

    public long getActualCount()
    {
        return actualCount;
    }

    public long getErrorCount()
    {
        return errorCount;
    }

    public long getMaxCount()
    {
        return maxCount;
    }

    /**
     * Calculates the actual progress in percent.
     * @return The actual progress in percent.
     */
    public float calcProgressInPercent()
    {
        return actualCount * 100 / maxCount;
    }

    /**
     * Calculates the error percentage of actual progress.
     * @return The error percentage of actual progress.
     */
    public float calcActualErrorQuoteInPercent()
    {
        return errorCount * 100 / actualCount;
    }

    /**
     * Calculates the total error percentage.
     * @return The total error percentage.
     */
    public float calcProgressErrorQuoteInPercent()
    {
        return errorCount * 100 / maxCount;
    }
}
