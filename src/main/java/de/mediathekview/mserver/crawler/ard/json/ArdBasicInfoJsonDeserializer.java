package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Converts json with basic information from http://www.ardmediathek.de/play/sola/[documentId] to {@link ArdBasicInfoDTO}.
 */
public class ArdBasicInfoJsonDeserializer implements JsonDeserializer<ArdBasicInfoDTO>
{

    private static final String ELEMENT_METADATA = "metadata";
    private static final String ELEMENT_CATEGORY = "category";
    private static final String ELEMENT_SHOW = "show";
    private static final String ELEMENT_TITLE = "title";

    @Override
    public ArdBasicInfoDTO deserialize(final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException
    {
        ArdBasicInfoDTO dto = new ArdBasicInfoDTO();
        final JsonObject metadataObject = aJsonElement.getAsJsonObject().get(ELEMENT_METADATA).getAsJsonObject();
        dto.setSenderName(metadataObject.get(ELEMENT_CATEGORY).getAsString());
        dto.setThema(metadataObject.get(ELEMENT_SHOW).getAsString());
        dto.setTitle(metadataObject.get(ELEMENT_TITLE).getAsString());
        return dto;
    }
}
