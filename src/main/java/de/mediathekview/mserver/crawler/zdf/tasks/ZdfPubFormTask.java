package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.*;
import de.mediathekview.mserver.crawler.zdf.*;
import de.mediathekview.mserver.crawler.zdf.json.ZdfPubFormDeserializer;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfPubFormTask extends ZdfTaskBase<ZdfPubFormResult, ZdfPubFormDto> {

  private static final Type SET_TOPICURL_TYPE_TOKEN =
      new TypeToken<ZdfPubFormResult>() {}.getType();

  public ZdfPubFormTask(
      AbstractCrawler aCrawler, Queue<ZdfPubFormDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
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
      final Queue<ZdfPubFormDto> urls = new ConcurrentLinkedQueue<>();
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
  protected AbstractRecursiveConverterTask<ZdfPubFormResult, ZdfPubFormDto> createNewOwnInstance(
      Queue<ZdfPubFormDto> aElementsToProcess) {
    return new ZdfPubFormTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
