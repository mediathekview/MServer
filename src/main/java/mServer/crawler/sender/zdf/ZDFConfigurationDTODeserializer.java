package mServer.crawler.sender.zdf;

import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOG = LogManager.getLogger(ZDFConfigurationDTODeserializer.class);
    private static final String FALLBACK_TOKEN = "69c4eddbe0cf82b2a9277e8106a711db314a3008";
    public static final String JSON_ELEMENT_API_TOKEN = "apiToken";

    @Override
    public ZDFConfigurationDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
        ZDFConfigurationDTO dto = null;
        try {
            JsonObject targetObject = aJsonElement.getAsJsonObject();
            JsonElement apiTokenElement = targetObject.get(JSON_ELEMENT_API_TOKEN);
            
            String apiToken;
            if(apiTokenElement == null || apiTokenElement.isJsonNull())
            {
                LOG.error("Can't load the API Token for ZDF. Using the fallback.");
                apiToken = FALLBACK_TOKEN;
            }else {
                apiToken = apiTokenElement.getAsString();
                
            }

            

            dto = new ZDFConfigurationDTO(apiToken);
        } catch (Exception ex) {
            Log.errorLog(496583255, ex);
        }

        return dto;
    }
}
