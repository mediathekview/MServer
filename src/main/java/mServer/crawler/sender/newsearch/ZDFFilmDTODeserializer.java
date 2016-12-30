package mServer.crawler.sender.newsearch;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by nicklas on 30.12.16.
 */
public class ZDFFilmDTODeserializer implements JsonDeserializer<ZDFFilmDTO>
{

    public static final String JSON_ELEMENT_DURATION = "duration";
    public static final String JSON_ELEMENT_GEO_LOCATION = "geoLocation";
    public static final String JSON_ELEMENT_VIDEO_INFORMATION_URL = "http://zdf.de/rels/streams/ptmd-template";
    public static final String JSON_ELEMENT_TITLE = "title";
    public static final String JSON_ELEMENT_ALT_TEXT = "altText";
    public static final String JSON_ELEMENT_CAPTION = "caption";
    public static final String JSON_OBJ_ELEMENT_TARGET = "http://zdf.de/rels/target";
    public static final String JSON_OBJ_ELEMENT_TEASER_IMAGE_REF = "teaserImageRef";
    public static final String JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT = "mainVideoContent";

    @Override
    public ZDFFilmDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException
    {
        JsonObject targetObject = aJsonElement.getAsJsonObject().getAsJsonObject(JSON_OBJ_ELEMENT_TARGET);

        JsonObject teaserImageRefObject = targetObject.getAsJsonObject(JSON_OBJ_ELEMENT_TEASER_IMAGE_REF);
        JsonObject mainVideoContentObject = targetObject.getAsJsonObject(JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT).getAsJsonObject(JSON_OBJ_ELEMENT_TARGET);

        ZDFFilmDTO zdfFilmDTO = new ZDFFilmDTO(
                mainVideoContentObject.get(JSON_ELEMENT_DURATION).getAsInt(),
                mainVideoContentObject.get(JSON_ELEMENT_GEO_LOCATION).getAsString(),
                mainVideoContentObject.get(JSON_ELEMENT_VIDEO_INFORMATION_URL).getAsString(),
                teaserImageRefObject.get(JSON_ELEMENT_TITLE).getAsString(),
                teaserImageRefObject.get(JSON_ELEMENT_ALT_TEXT).getAsString(),
                teaserImageRefObject.get(JSON_ELEMENT_CAPTION).getAsString()
        );

        return zdfFilmDTO;
    }
}
