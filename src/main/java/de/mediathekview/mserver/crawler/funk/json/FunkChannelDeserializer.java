package de.mediathekview.mserver.crawler.funk.json;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;

import java.util.Optional;

public class FunkChannelDeserializer extends AbstractFunkElementDeserializer<FunkChannelDTO> {
  private static final String TAG_CHANNEL_DTO_LIST = "channelDTOList";
  private static final String TAG_TITLE = "title";
  private static final String TAG_ENTITY_ID = "entityId";

  public FunkChannelDeserializer(final MServerBasicConfigDTO aSenderConfig) {
    super(aSenderConfig);
  }

  public FunkChannelDeserializer(
      final Optional<AbstractCrawler> aCrawler, final MServerBasicConfigDTO aSenderConfig) {
    super(aCrawler, aSenderConfig);
  }

  @Override
  protected FunkChannelDTO mapToElement(final JsonObject jsonObject) {
    return new FunkChannelDTO(
        jsonObject.get(TAG_ENTITY_ID).getAsString(), jsonObject.get(TAG_TITLE).getAsString());
  }

  @Override
  protected String[] getRequiredTags() {
    return new String[] {TAG_TITLE, TAG_ENTITY_ID};
  }

  @Override
  protected String getElementListTag() {
    return TAG_CHANNEL_DTO_LIST;
  }
}
