package mServer.crawler.sender.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.zdf.ZdfConstants;
import mServer.crawler.sender.zdf.ZdfPubFormDto;
import mServer.crawler.sender.zdf.ZdfPubFormResult;
import mServer.crawler.sender.zdf.ZdfUrlBuilder;
import mServer.crawler.sender.zdf.json.ZdfPubFormDeserializer;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfPubFormTask extends ZdfTaskBase<ZdfPubFormResult, ZdfPubFormDto> {

  private static final Type SET_TOPICURL_TYPE_TOKEN =
      new TypeToken<ZdfPubFormResult>() {}.getType();

  public ZdfPubFormTask(
          MediathekReader aCrawler, ConcurrentLinkedQueue<ZdfPubFormDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(authKey));
    registerJsonDeserializer(SET_TOPICURL_TYPE_TOKEN, new ZdfPubFormDeserializer());
  }

  @Override
  protected void processRestTarget(ZdfPubFormDto aDTO, WebTarget aTarget) {
    final ZdfPubFormResult actual = deserialize(aTarget, SET_TOPICURL_TYPE_TOKEN);
    if (actual != null) {
      actual.getFilms().forEach(film -> film.setTopic(aDTO.getTopic()));
      taskResults.add(actual);
      processNextPage(aDTO.getTopic(), aDTO.getCollectionId(), actual.getTopics().getNextPage());
    }
  }

  private void processNextPage(String topic, String collectionId, final Optional<String> cursor) {
    if (cursor.isPresent() && !cursor.get().isEmpty()) {
      final ConcurrentLinkedQueue<ZdfPubFormDto> urls = new ConcurrentLinkedQueue<>();
      urls.add(
          new ZdfPubFormDto(
              topic,
              collectionId,
              ZdfUrlBuilder.buildTopicNoSeasonUrl(
                  ZdfConstants.EPISODES_PAGE_SIZE, collectionId, cursor.get())));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }

  @Override
  protected AbstractRestTask<ZdfPubFormResult, ZdfPubFormDto> createNewOwnInstance(
      ConcurrentLinkedQueue<ZdfPubFormDto> aElementsToProcess) {
    return new ZdfPubFormTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
