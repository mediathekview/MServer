package mServer.crawler.sender.kika.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.kika.KikaEntityDto;

import java.lang.reflect.Type;
import java.util.Optional;

public class KikaBrandPageDeserializer implements JsonDeserializer<Optional<KikaEntityDto>> {
  private static final String TAG_TYPE = "docType";
  private static final String TAG_ID = "id";
  private static final String TAG_UUID = "uuid";
  private static final String TAG_EXTERNALID = "externalId";
  private static final String TAG_URL_PATH = "urlPath";
  private static final String TAG_MODIFICATIONDATE = "modificationDate";
  private static final String[] TAG_API_ID = new String[] {"videoSubchannel","id"};
  private static final String[] TAG_API_URL = new String[] {"videoSubchannel","videosPageUrl"};
  
  
  @Override
  public Optional<KikaEntityDto> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    if (JsonUtils.getElementValueAsString(jsonElement, TAG_API_URL).isPresent()) {
      final KikaEntityDto e = new KikaEntityDto(
          JsonUtils.getElementValueAsString(jsonElement, TAG_TYPE),
          JsonUtils.getElementValueAsString(jsonElement, TAG_ID),
          JsonUtils.getElementValueAsString(jsonElement, TAG_UUID),
          JsonUtils.getElementValueAsString(jsonElement, TAG_EXTERNALID),
          JsonUtils.getElementValueAsString(jsonElement, TAG_URL_PATH),
          JsonUtils.getElementValueAsString(jsonElement, TAG_API_ID),
          JsonUtils.getElementValueAsString(jsonElement, TAG_API_URL),
          JsonUtils.getElementValueAsString(jsonElement, TAG_MODIFICATIONDATE)
          );
      return Optional.of(e);
    }
    
    return Optional.empty();
  }
  

}
