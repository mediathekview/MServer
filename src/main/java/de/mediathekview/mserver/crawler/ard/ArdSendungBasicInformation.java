package de.mediathekview.mserver.crawler.ard;

/**
 * Basic information about ARD Sendungen.
 */
public class ArdSendungBasicInformation
{
    private String url;
    private String sendezeitAsText;

    public ArdSendungBasicInformation(final String url, final String sendezeitAsText)
    {
        this.url = url;
        this.sendezeitAsText = sendezeitAsText;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }
}
