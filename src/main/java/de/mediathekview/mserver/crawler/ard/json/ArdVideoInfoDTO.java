package de.mediathekview.mserver.crawler.ard.json;

import java.util.HashMap;
import java.util.Map;

import de.mediathekview.mlib.daten.Qualities;

/**
 * Video information from
 * http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash.
 */
public class ArdVideoInfoDTO
{
    private final Map<Qualities, String> videoUrls;
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

    public Qualities getDefaultQuality()
    {
        if (videoUrls.containsKey(Qualities.NORMAL))
        {
            return Qualities.NORMAL;
        }
        return videoUrls.keySet().iterator().next();
    }

    public String getDefaultVideoUrl()
    {
        return videoUrls.get(getDefaultQuality());
    }

    public String put(final Qualities key, final String value)
    {
        return videoUrls.put(key, value);
    }
}
