package mServer.tool;

import mServer.crawler.sender.newsearch.Qualities;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class with some Utils to work with M3U8 urls.
 */
public class M3U8Utils
{
    public static final String M3U8_WDR_URL_BEGIN = "http://adaptiv.wdr.de/i/medp/";
    private static final String REGEX_FIRST_USELESS_COMMA = "^,";
    private static final String M3U8_WDR_QUALITIES_USELESS_END = ",.mp4.csmil";
    public static final String WDR_MP4_URL_PATTERN = "http://ondemand-%s.wdr.de/medp/%s/%s/%s/%s.mp4";

    private M3U8Utils()
    {
        super();
    }

    /**
     * If the URL follows the following structure it will be used to generate MP4 URLS from it.<br>
     * This structure is needed: <code>http://adaptiv.wdr.de/i/medp/[region]/[fsk]/[unkownNumber]/[videoId]/,[Qualitie 01],[Qualitie 02],[Qualitie ...],.mp4.csmil/master.m3u8</code><br>
     *
     * @param aWDRM3U8Url The M3U8 URL.
     * @return A Map containing the URLs and Qualities which was found. An empty Map if nothing was found.
     */
    public static Map<Qualities, String> gatherUrlsFromWDRM3U8(String aWDRM3U8Url)
    {
        Map<Qualities, String> urlAndQualitiess = new HashMap<>();
        if (aWDRM3U8Url.startsWith(M3U8_WDR_URL_BEGIN))
        {
            String m3u8Url = aWDRM3U8Url.replace(M3U8_WDR_URL_BEGIN, "");
            String[] splittedM3U8Url = StringUtils.split(m3u8Url, '/');
            if (splittedM3U8Url.length >= 6)
            {
                String region = splittedM3U8Url[0];
                String fsk = splittedM3U8Url[1];
                String unkownNumber = splittedM3U8Url[2];
                String videoId = splittedM3U8Url[3];
                String urlQualityPartsText = splittedM3U8Url[4]; // From lowest (left) to best (right) quality;
                if (StringUtils.isNoneEmpty(region, fsk, unkownNumber, videoId, urlQualityPartsText))
                {
                    //Remove useless begin and end.
                    urlQualityPartsText = urlQualityPartsText.replaceFirst(REGEX_FIRST_USELESS_COMMA, "").replaceFirst(M3U8_WDR_QUALITIES_USELESS_END, "");

                    List<String> urlQualityParts = Arrays.asList(StringUtils.split(urlQualityPartsText, ','));
                    if (urlQualityParts.size() == 1)
                    {
                        urlAndQualitiess.put(Qualities.SMALL, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, urlQualityParts.get(0)));
                    } else if (urlQualityParts.size() == 2)
                    {
                        urlAndQualitiess.put(Qualities.SMALL, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, urlQualityParts.get(0)));
                        urlAndQualitiess.put(Qualities.NORMAL, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, urlQualityParts.get(1)));
                    } else if (urlQualityParts.size() >= 3)
                    {
                        List<String> bestThreeUrlQualityParts = urlQualityParts.subList(urlQualityParts.size() - 3, urlQualityParts.size());
                        urlAndQualitiess.put(Qualities.SMALL, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, bestThreeUrlQualityParts.get(0)));
                        urlAndQualitiess.put(Qualities.NORMAL, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, bestThreeUrlQualityParts.get(1)));
                        urlAndQualitiess.put(Qualities.HD, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, bestThreeUrlQualityParts.get(2)));
                    }
                }
                //The remaining is irrelevant.
            }
        }
        return urlAndQualitiess;
    }
}
