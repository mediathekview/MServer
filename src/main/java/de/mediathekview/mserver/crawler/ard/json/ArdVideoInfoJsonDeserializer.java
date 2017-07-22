package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.*;
import de.mediathekview.mlib.daten.Qualities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

/**
 * Converts json with basic video from http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash to a map of {@link Qualities} with corresponding urls.
 */
public class ArdVideoInfoJsonDeserializer implements JsonDeserializer<ArdVideoInfoDTO>
{
    private static final Logger LOG = LogManager.getLogger(ArdVideoInfoJsonDeserializer.class);
    private static final String ELEMENT_STREAM = "_stream";
    private static final String ELEMENT_MEDIA_ARRAY = "_mediaArray";
    private static final String ELEMENT_MEDIA_STREAM_ARRAY = "_mediaStreamArray";
    private static final String ELEMENT_SUBTITLE_URL = "_subtitleUrl";
    private static final String ELEMENT_QUALITY = "_quality";

    @Override
    public ArdVideoInfoDTO deserialize(final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException
    {
        ArdVideoInfoDTO videoInfo = new ArdVideoInfoDTO();
        final JsonElement subtitleElement = aJsonElement.getAsJsonObject().get(ELEMENT_SUBTITLE_URL);
        if (subtitleElement != null)
        {
            videoInfo.setSubtitleUrl(subtitleElement.getAsString());
        }

        final JsonArray mediaStreamArray = aJsonElement.getAsJsonObject()
                .getAsJsonArray(ELEMENT_MEDIA_ARRAY).get(0)
                .getAsJsonObject()
                .getAsJsonArray(ELEMENT_MEDIA_STREAM_ARRAY);

        for (int i = 0; i < mediaStreamArray.size(); i++)
        {
            final JsonElement vidoeElement = mediaStreamArray.get(i);
            String qualityAsText = vidoeElement.getAsJsonObject().get(ELEMENT_QUALITY).getAsString();

            int qualityNumber;
            try
            {
                qualityNumber = Integer.parseInt(qualityAsText);
            } catch (NumberFormatException numberFormatException)
            {
                LOG.debug("Can't convert quality %s to an integer.", qualityAsText, numberFormatException);
                qualityNumber = -1;
            }

            if(qualityNumber > 0 || mediaStreamArray.size() == 1)
            {
                Qualities quality = getQualityForNumber(qualityNumber);
                videoInfo.put(quality, videoElementToUrl(vidoeElement));
            }
        }


        return videoInfo;
    }

    private Qualities getQualityForNumber(final int i)
    {
        switch (i)
        {
            case 0:
                return Qualities.VERY_SMALL;

            case 1:
                return Qualities.SMALL;

            case 2:
            default:
                return Qualities.NORMAL;

            case 3:
                return Qualities.HD;
        }
    }

    private String videoElementToUrl(final JsonElement aVideoElement)
    {
        return aVideoElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString();
    }
}
