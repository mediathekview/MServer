package de.mediathekview.mserver.crawler.basic;

public class CrawlerUrlsDTO {
    private String url;

    public CrawlerUrlsDTO(final String aUrl)
    {
        url = aUrl;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String aUrl)
    {
        url = aUrl;
    }
    
}
