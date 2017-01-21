package mServer.crawler.sender.newsearch;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * A JSON deserializer to gather the needed information for a {@link ZDFEntryDTO}.
 */
public class ZDFEntryDTODeserializer implements JsonDeserializer<ZDFEntryDTO>
{

    public static final String JSON_ELEMENT_DOWNLOAD_INFORMATION_URL = "http://zdf.de/rels/streams/ptmd-template";
    public static final String JSON_OBJ_ELEMENT_TARGET = "http://zdf.de/rels/target";
    public static final String JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT = "mainVideoContent";
    public static final String JSON_ELEMENT_GENERAL_INFORMATION_URL = "canonical";

    @Override
    public ZDFEntryDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException
    {
        JsonObject targetObject = aJsonElement.getAsJsonObject().getAsJsonObject(JSON_OBJ_ELEMENT_TARGET);
        JsonObject mainVideoContentObject = targetObject.getAsJsonObject(JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT).getAsJsonObject(JSON_OBJ_ELEMENT_TARGET);
        final JsonElement entryGeneralInformationUrlElement = targetObject.get(JSON_ELEMENT_GENERAL_INFORMATION_URL);
        final JsonElement entryDownloadInformationUrlElement = mainVideoContentObject.get(JSON_ELEMENT_DOWNLOAD_INFORMATION_URL);

        String downloadUrl = entryDownloadInformationUrlElement.getAsString()
                .replace("{playerId}", "ngplayer_2_3");

        return new ZDFEntryDTO(entryGeneralInformationUrlElement.getAsString(), downloadUrl);
    }
}
