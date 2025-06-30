package mServer.crawler.sender.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.zdf.ZdfLetterDto;
import mServer.crawler.sender.zdf.ZdfTopicUrlDto;
import mServer.crawler.sender.zdf.ZdfUrlBuilder;
import mServer.crawler.sender.zdf.json.ZdfLetterPageDeserializer;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfLetterPageTask extends ZdfTaskBase<ZdfTopicUrlDto, ZdfLetterDto> {

  private static final Type SET_TOPICURL_TYPE_TOKEN =
      new TypeToken<PagedElementListDTO<ZdfTopicUrlDto>>() {}.getType();

  public ZdfLetterPageTask(
          MediathekReader aCrawler, ConcurrentLinkedQueue<ZdfLetterDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(authKey));
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
      final ConcurrentLinkedQueue<ZdfLetterDto> urls = new ConcurrentLinkedQueue<>();
      urls.add(new ZdfLetterDto(index, ZdfUrlBuilder.buildLetterPageUrl(cursor.get(), index)));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }

  @Override
  protected AbstractRestTask<ZdfTopicUrlDto, ZdfLetterDto> createNewOwnInstance(
      ConcurrentLinkedQueue<ZdfLetterDto> aElementsToProcess) {
    return new ZdfLetterPageTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
