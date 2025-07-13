package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.*;
import de.mediathekview.mserver.crawler.zdf.ZdfLetterDto;
import de.mediathekview.mserver.crawler.zdf.ZdfTopicUrlDto;
import de.mediathekview.mserver.crawler.zdf.ZdfUrlBuilder;
import de.mediathekview.mserver.crawler.zdf.json.ZdfLetterPageDeserializer;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfLetterPageTask extends ZdfTaskBase<ZdfTopicUrlDto, ZdfLetterDto> {

  private static final Type SET_TOPICURL_TYPE_TOKEN =
      new TypeToken<PagedElementListDTO<ZdfTopicUrlDto>>() {}.getType();

  public ZdfLetterPageTask(
          AbstractCrawler aCrawler, Queue<ZdfLetterDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    registerJsonDeserializer(SET_TOPICURL_TYPE_TOKEN, new ZdfLetterPageDeserializer());
  }

  @Override
  protected void processRestTarget(ZdfLetterDto aDTO, WebTarget aTarget) {
    final PagedElementListDTO<ZdfTopicUrlDto> actual = deserialize(aTarget, SET_TOPICURL_TYPE_TOKEN);
    if (actual != null) {
      taskResults.addAll(actual.getElements());
      processNextPage(aDTO.getIndex(), actual.getNextPage());
    }
  }

  private void processNextPage(int index, final Optional<String> cursor) {
    if (cursor.isPresent() && !cursor.get().isEmpty()) {
      final Queue<ZdfLetterDto> urls = new ConcurrentLinkedQueue<>();
      urls.add(new ZdfLetterDto(index, ZdfUrlBuilder.buildLetterPageUrl(cursor.get(), index)));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<ZdfTopicUrlDto, ZdfLetterDto> createNewOwnInstance(
      Queue<ZdfLetterDto> aElementsToProcess) {
    return new ZdfLetterPageTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
