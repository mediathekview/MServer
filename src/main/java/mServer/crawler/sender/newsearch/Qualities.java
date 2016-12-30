package mServer.crawler.sender.newsearch;

public enum Qualities
{
    HD("HD"), NORMAL("Normal"), SMALL("Klein");

    private final String description;

    Qualities(String aDescription)
    {
        description = aDescription;
    }

    public String getDescription()
    {
        return description;
    }
}
