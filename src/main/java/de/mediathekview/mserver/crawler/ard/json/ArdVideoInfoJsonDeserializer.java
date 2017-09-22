package de.mediathekview.mserver.crawler.ard.json;

import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import de.mediathekview.mlib.daten.Resolution;

/**
 * Converts json with basic video from
 * http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash
 * to a map of {@link Resolution} with corresponding urls.
 */
public class ArdVideoInfoJsonDeserializer implements JsonDeserializer<ArdVideoInfoDTO>
{
    private static final String PROTOCOL_RTMP = "rtmp";
    private static final Logger LOG = LogManager.getLogger(ArdVideoInfoJsonDeserializer.class);
    private static final String ELEMENT_STREAM = "_stream";
    private static final String ELEMENT_MEDIA_ARRAY = "_mediaArray";
    private static final String ELEMENT_MEDIA_STREAM_ARRAY = "_mediaStreamArray";
    private static final String ELEMENT_SUBTITLE_URL = "_subtitleUrl";
    private static final String ELEMENT_QUALITY = "_quality";
    private static final String URL_PREFIX_PATTERN = "\\w+:";
    private static final String URL_PATTERN = "\\w+.*";
    private static final String ELEMENT_SERVER = "_server";

    @Override
    public ArdVideoInfoDTO deserialize(final JsonElement aJsonElement, final Type aType,
            final JsonDeserializationContext aJsonDeserializationContext)
    {
        final ArdVideoInfoDTO videoInfo = new ArdVideoInfoDTO();
        final JsonElement subtitleElement = aJsonElement.getAsJsonObject().get(ELEMENT_SUBTITLE_URL);
        if (subtitleElement != null)
        {
            videoInfo.setSubtitleUrl(subtitleElement.getAsString());
        }

        final JsonArray mediaStreamArray = aJsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_MEDIA_ARRAY).get(0)
                .getAsJsonObject().getAsJsonArray(ELEMENT_MEDIA_STREAM_ARRAY);

        for (int i = 0; i < mediaStreamArray.size(); i++)
        {
            final JsonElement vidoeElement = mediaStreamArray.get(i);
            final String qualityAsText = vidoeElement.getAsJsonObject().get(ELEMENT_QUALITY).getAsString();

            final String baseUrl = vidoeElement.getAsJsonObject().has(ELEMENT_SERVER)
                    ? vidoeElement.getAsJsonObject().get(ELEMENT_SERVER).getAsString()
                    : "";
            if (baseUrl.startsWith(PROTOCOL_RTMP))
            {
                LOG.debug("Found an Sendung with the old RTMP format: " + videoElementToUrl(vidoeElement, baseUrl));
            }
            else
            {
                int qualityNumber;
                try
                {
                    qualityNumber = Integer.parseInt(qualityAsText);
                }
                catch (final NumberFormatException numberFormatException)
                {
                    LOG.debug("Can't convert quality %s to an integer.", qualityAsText, numberFormatException);
                    qualityNumber = -1;
                }

                if (qualityNumber > 0 || mediaStreamArray.size() == 1)
                {
                    final Resolution quality = getQualityForNumber(qualityNumber);
                    videoInfo.put(quality, videoElementToUrl(vidoeElement, baseUrl));
                }
            }
        }
        return videoInfo;
    }

    private Resolution getQualityForNumber(final int i)
    {
        switch (i)
        {
        case 0:
            return Resolution.VERY_SMALL;

        case 1:
            return Resolution.SMALL;

        case 2:
        default:
            return Resolution.NORMAL;

        case 3:
            return Resolution.HD;
        }
    }

    private String videoElementToUrl(final JsonElement aVideoElement, final String aBaseUrl)
    {
        String url = aVideoElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString();
        if (url.matches(URL_PREFIX_PATTERN + URL_PATTERN))
        {
            url = url.replaceFirst(URL_PREFIX_PATTERN, aBaseUrl);
        }
        return url;
    }
}
