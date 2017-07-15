package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.*;
import de.mediathekview.mlib.daten.Qualities;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts json with basic video from http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash to a map of {@link Qualities} with corresponding urls.
 */
public class ArdVideoInfoJsonDeserializer implements JsonDeserializer<ArdVideoInfoDTO>
{

    private static final String ELEMENT_STREAM = "_stream";
    private static final String ELEMENT_MEDIA_ARRAY = "_mediaArray";
    private static final String ELEMENT_MEDIA_STREAM_ARRAY = "_mediaStreamArray";
    private static final String ELEMENT_SUBTITLE_URL = "_subtitleUrl";

    @Override
    public ArdVideoInfoDTO deserialize(final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException
    {
        ArdVideoInfoDTO videoInfo = new ArdVideoInfoDTO();
        final JsonElement subtitleElement = aJsonElement.getAsJsonObject().get(ELEMENT_SUBTITLE_URL);
        if(subtitleElement != null)
        {
            videoInfo.setSubtitleUrl(subtitleElement.getAsString());
        }

        final JsonArray mediaStreamArray = aJsonElement.getAsJsonObject()
                .getAsJsonArray(ELEMENT_MEDIA_ARRAY).get(0)
                .getAsJsonObject()
                .getAsJsonArray(ELEMENT_MEDIA_STREAM_ARRAY);

        for (int i = 1; i < mediaStreamArray.size() && i <= 4; i++)
        {
            final JsonElement vidoeElement = mediaStreamArray.get(i);

            Qualities quality = getQuality(i);
            videoInfo.put(quality, videoElementToUrl(vidoeElement));
        }


        return videoInfo;
    }

    private Qualities getQuality(final int i)
    {
        switch (i)
        {
            case 1:
                return Qualities.VERY_SMALL;

            case 2:
                return Qualities.SMALL;

            case 3:
                return Qualities.NORMAL;

            case 4:
                return Qualities.HD;

            default:
                return null;
        }
    }

    private String videoElementToUrl(final JsonElement aVideoElement)
    {
        return aVideoElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString();
    }
}
