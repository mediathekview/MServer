package mServer.crawler.sender.newsearch;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mediathekview.mlib.tool.Log;

/**
 * A JSON deserializer to gather the needed information for a {@link ZDFConfigurationDTO}.
 */
public class ZDFConfigurationDTODeserializer implements JsonDeserializer<ZDFConfigurationDTO> {

    public static final String JSON_ELEMENT_API_TOKEN = "apiToken";

    @Override
    public ZDFConfigurationDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
        ZDFConfigurationDTO dto = null;
        try {
            JsonObject targetObject = aJsonElement.getAsJsonObject();
            JsonElement apiTokenElement = targetObject.get(JSON_ELEMENT_API_TOKEN);

            String apiToken = apiTokenElement.getAsString();

            dto = new ZDFConfigurationDTO(apiToken);
        } catch (Exception ex) {
            Log.errorLog(496583255, ex);
        }

        return dto;
    }
}
