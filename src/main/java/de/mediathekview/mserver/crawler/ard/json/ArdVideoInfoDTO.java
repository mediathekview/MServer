package de.mediathekview.mserver.crawler.ard.json;

import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;

import java.util.HashMap;
import java.util.Map;

/**
 *  Video information from http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash.
 */
public class ArdVideoInfoDTO
{
    private Map<Qualities,String> videoUrls;
    private String subtitleUrl;

    public ArdVideoInfoDTO()
    {
        videoUrls = new HashMap<>();
    }

    public String getSubtitleUrl()
    {
        return subtitleUrl;
    }

    public void setSubtitleUrl(final String subtitleUrl)
    {
        this.subtitleUrl = subtitleUrl;
    }

    public Map<Qualities, String> getVideoUrls()
    {
        return videoUrls;
    }

    public String put(final Qualities key, final String value)
    {
        return videoUrls.put(key, value);
    }
}
