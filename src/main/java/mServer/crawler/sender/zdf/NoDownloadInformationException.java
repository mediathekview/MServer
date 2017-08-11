package mServer.crawler.sender.zdf;

public class NoDownloadInformationException extends Exception
{
    public NoDownloadInformationException()
    {
        super("A film without any download information was found.");
    }
}
