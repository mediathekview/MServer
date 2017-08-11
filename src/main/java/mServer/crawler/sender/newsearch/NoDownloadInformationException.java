package mServer.crawler.sender.newsearch;

public class NoDownloadInformationException extends Exception
{
    public NoDownloadInformationException()
    {
        super("A film without any download information was found.");
    }
}
