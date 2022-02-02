package mServer.crawler.sender.funk.json;

import com.google.gson.JsonObject;
import mServer.crawler.sender.funk.FunkChannelDTO;

public class FunkChannelDeserializer extends AbstractFunkElementDeserializer<FunkChannelDTO> {
  private static final String TAG_CHANNEL_DTO_LIST = "channelDTOList";
  private static final String TAG_TITLE = "title";
  private static final String TAG_ENTITY_ID = "entityId";

  @Override
  protected FunkChannelDTO mapToElement(final JsonObject jsonObject) {
    return new FunkChannelDTO(
            jsonObject.get(TAG_ENTITY_ID).getAsString(), jsonObject.get(TAG_TITLE).getAsString());
  }

  @Override
  protected String[] getRequiredTags() {
    return new String[]{TAG_TITLE, TAG_ENTITY_ID};
  }

  @Override
  protected String getElementListTag() {
    return TAG_CHANNEL_DTO_LIST;
  }
}
